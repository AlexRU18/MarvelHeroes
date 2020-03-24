/*
 * Designed and developed by 2020 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.marvelheroes.repository

import androidx.lifecycle.MutableLiveData
import com.skydoves.marvelheroes.model.Poster
import com.skydoves.marvelheroes.network.ApiResponse
import com.skydoves.marvelheroes.network.MarvelClient
import com.skydoves.marvelheroes.network.message
import com.skydoves.marvelheroes.persistence.PosterDao
import com.skydoves.whatif.whatIfNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainRepository constructor(
  private val marvelClient: MarvelClient,
  private val posterDao: PosterDao
) : Repository {

  override var isLoading = false

  init {
    Timber.d("Injection MainRepository")
  }

  suspend fun loadMarvelPosters(error: (String) -> Unit) = withContext(Dispatchers.IO) {
    val liveData = MutableLiveData<List<Poster>>()
    var posters = posterDao.getPosterList()
    if (posters.isEmpty()) {
      isLoading = true
      marvelClient.fetchMarvelPosters { response ->
        isLoading = false
        when (response) {
          is ApiResponse.Success -> {
            response.data.whatIfNotNull {
              posters = it
              liveData.postValue(it)
              posterDao.insertPosterList(it)
            }
          }
          is ApiResponse.Failure.Error -> error(response.message())
          is ApiResponse.Failure.Exception -> error(response.message())
        }
      }
    }
    liveData.apply { postValue(posters) }
  }
}
