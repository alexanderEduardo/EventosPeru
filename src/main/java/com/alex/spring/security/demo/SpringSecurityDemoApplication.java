package com.alex.spring.security.demo;

import com.alex.spring.security.demo.persistence.entity.PermissionEntity;
import com.alex.spring.security.demo.persistence.entity.RoleEntity;
import com.alex.spring.security.demo.persistence.entity.RoleEnum;
import com.alex.spring.security.demo.persistence.entity.UserEntity;
import com.alex.spring.security.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class SpringSecurityDemoApplication {

	public SpringSecurityDemoApplication(UserRepository repository, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityDemoApplication.class, args);
	}

	final UserRepository repository;
	final PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner init(UserRepository userRepository){
		return args -> {
			PermissionEntity createPermission = PermissionEntity.builder()
					.name("CREATE")
					.build();
			PermissionEntity readPermission = PermissionEntity.builder()
					.name("READ")
					.build();
			PermissionEntity updatePermission = PermissionEntity.builder()
					.name("UPDATE")
					.build();
			PermissionEntity deletePermission = PermissionEntity.builder()
					.name("DELETE")
					.build();

			RoleEntity roleAdmin = RoleEntity.builder()
					.roleEnum(RoleEnum.ADMIN)
					.permissionEntities(Set.of(createPermission,readPermission,updatePermission,deletePermission))
					.build();
			RoleEntity roleDev = RoleEntity.builder()
					.roleEnum(RoleEnum.DEV)
					.permissionEntities(Set.of(createPermission,readPermission,updatePermission))
					.build();
			RoleEntity roleUser = RoleEntity.builder()
					.roleEnum(RoleEnum.USER)
					.permissionEntities(Set.of(createPermission,readPermission))
					.build();
			RoleEntity roleInvited = RoleEntity.builder()
					.roleEnum(RoleEnum.INVITED)
					.permissionEntities(Set.of(readPermission))
					.build();

			String passwordEncripted = passwordEncoder.encode("1234");
			UserEntity admin = UserEntity.builder()
					.username("admin")
					.password(passwordEncoder.encode("1234"))
					.accountNoExpired(true)
					.accountNoLocked(true)
					.credentialNoExpired(true)
					.isEnabled(true)
					.roleEntitySet(Set.of(roleAdmin))
					.build();

			UserEntity alex = UserEntity.builder()
					.username("alex")
					.password(passwordEncoder.encode("1234"))
					.accountNoExpired(true)
					.accountNoLocked(true)
					.credentialNoExpired(true)
					.isEnabled(false)
					.roleEntitySet(Set.of(roleDev))
					.build();

			UserEntity pepe = UserEntity.builder()
					.username("pepe")
					.password(passwordEncoder.encode("1234"))
					.accountNoExpired(true)
					.accountNoLocked(true)
					.credentialNoExpired(true)
					.isEnabled(true)
					.roleEntitySet(Set.of(roleUser))
					.build();

			UserEntity jonas = UserEntity.builder()
					.username("jonas")
					.password(passwordEncoder.encode("1234"))
					.accountNoExpired(true)
					.accountNoLocked(true)
					.credentialNoExpired(true)
					.isEnabled(true)
					.roleEntitySet(Set.of(roleInvited))
					.build();

			repository.saveAll(List.of(admin,alex,pepe,jonas));
		};
	}

}
