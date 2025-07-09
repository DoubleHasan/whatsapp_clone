package com.example.whatsapp

import android.content.Context
import android.content.SharedPreferences
import com.example.whatsapp.usecase.ContactRepository
import com.example.whatsapp.usecase.ContactUseCaseImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltInjection {
    @Provides
    @Singleton
    fun provideFirebaseAuth() : FirebaseAuth{
        return FirebaseAuth.getInstance()
    }
    @Provides
    fun provideContactRepository(
        @ApplicationContext context: Context
    ): ContactRepository {
        return ContactUseCaseImpl(context)
    }
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context) : SharedPreferences{
        return context.getSharedPreferences("Phone", Context.MODE_PRIVATE)
    }
    @Provides
    @Singleton
    fun provideFirebaseFirestore() : FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

}