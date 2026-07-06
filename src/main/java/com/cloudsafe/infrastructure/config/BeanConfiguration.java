package com.cloudsafe.infrastructure.config;

import com.cloudsafe.application.usecase.CreateBackupUseCase;
import com.cloudsafe.application.usecase.RegisterUserUseCase;
import com.cloudsafe.domain.repository.BackupJobRepositoryPort;
import com.cloudsafe.domain.repository.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepositoryPort) {
        return new RegisterUserUseCase(userRepositoryPort);
    }

    @Bean
    public CreateBackupUseCase createBackupUseCase(BackupJobRepositoryPort backupJobRepositoryPort) {
        return new CreateBackupUseCase(backupJobRepositoryPort);
    }
}
