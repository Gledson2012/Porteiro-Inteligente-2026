package br.com.porteirointeligente.data.network

import br.com.porteirointeligente.data.network.dto.LoginRequest
import br.com.porteirointeligente.data.network.dto.LoginResponse
import br.com.porteirointeligente.data.network.dto.OwnerDto
import br.com.porteirointeligente.data.network.dto.RegisterRequest
import br.com.porteirointeligente.data.network.dto.VisitDto
import br.com.porteirointeligente.domain.model.Owner
import br.com.porteirointeligente.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // Auth
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Owners
    @GET("api/owners")
    suspend fun getOwners(): Response<List<OwnerDto>>

    @POST("api/owners")
    suspend fun addOwner(@Body owner: OwnerDto): Response<Owner>

    @PUT("api/owners/{id}")
    suspend fun updateOwner(@Path("id") id: Long, @Body owner: OwnerDto): Response<Owner>

    @DELETE("api/owners/{id}")
    suspend fun deleteOwner(@Path("id") id: Long): Response<Unit>

    // Visits
    @GET("api/visits")
    suspend fun getVisits(): Response<List<VisitDto>>

    @POST("api/visits")
    suspend fun addVisit(@Body visit: VisitDto): Response<VisitDto>

    @PUT("api/visits/{id}")
    suspend fun updateVisit(@Path("id") id: Long, @Body visit: VisitDto): Response<VisitDto>
}
