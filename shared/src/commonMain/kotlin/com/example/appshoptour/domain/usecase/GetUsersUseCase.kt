package com.example.appshoptour.domain.usecase

import com.example.appshoptour.domain.model.User
import com.example.appshoptour.domain.repository.UserRepository

/**
 * Use case — единица бизнес-логики.
 * operator fun invoke() позволяет вызывать как функцию: getUsersUseCase()
 */
class GetUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<List<User>> = repository.getUsers()
}
