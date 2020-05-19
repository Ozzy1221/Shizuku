package moe.shizuku.manager.management

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.authorization.AuthorizationManager
import moe.shizuku.manager.utils.BuildUtils
import moe.shizuku.manager.viewmodel.Resource
import moe.shizuku.redirectstorage.viewmodel.activitySharedViewModels
import moe.shizuku.redirectstorage.viewmodel.sharedViewModels
import java.util.*

@MainThread
fun ComponentActivity.appsViewModel() = sharedViewModels { AppsViewModel() }

@MainThread
fun Fragment.appsViewModel() = activitySharedViewModels { AppsViewModel() }

class AppsViewModel : ViewModel() {

    private val _packages = MutableLiveData<Resource<List<PackageInfo>>>()
    val packages = _packages as LiveData<Resource<List<PackageInfo>>>

    private val _grantedCount = MutableLiveData<Resource<Int>>()
    val grantedCount = _grantedCount as LiveData<Resource<Int>>

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                var count = 0
                for (pi in AuthorizationManager.getPackages(PackageManager.GET_META_DATA)) {
                    if (BuildConfig.APPLICATION_ID == pi.packageName) continue
                    if (pi?.applicationInfo?.metaData?.getBoolean("moe.shizuku.client.V3_SUPPORT") != true) continue
                    list.add(pi)
                    if (AuthorizationManager.granted(pi.packageName, pi.applicationInfo.uid)) count++
                }
                _packages.postValue(Resource.success(list))
                _grantedCount.postValue(Resource.success(count))
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _packages.postValue(Resource.error(e, null))
                _grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }

    fun loadCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list: MutableList<PackageInfo> = ArrayList()
                val packages: MutableList<String> = ArrayList()
                for (pi in AuthorizationManager.getPackages(PackageManager.GET_META_DATA)) {
                    if (BuildConfig.APPLICATION_ID == pi.packageName) continue
                    list.add(pi)
                    if (AuthorizationManager.granted(pi.packageName, pi.applicationInfo.uid)) packages.add(pi.packageName)
                }
            } catch (e: CancellationException) {

            } catch (e: Throwable) {
                _packages.postValue(Resource.error(e, null))
                _grantedCount.postValue(Resource.error(e, 0))
            }
        }
    }
}