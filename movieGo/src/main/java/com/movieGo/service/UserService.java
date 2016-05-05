package com.movieGo.service;

import java.util.Collections;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import com.movieGo.entity.User;
import com.movieGo.form.changePwForm;
import com.movieGo.repo.UserRepo;

import lombok.Getter;
import lombok.Setter;

@Service
@PropertySource("classpath:application.properties")
public class UserService implements UserDetailsService{
	
	@Autowired
    Environment env;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepo userRepo;
	
	@PostConstruct
	private void loadAdmin() {
		String adminMail = env.getProperty("admin.mail");
		String adminPw = env.getProperty("admin.pw");
		if (userRepo.findUserByMail(adminMail) == null) {
			User admin = new User(adminMail, passwordEncoder.encode(adminPw), User.ROLE_ADMIN);
			userRepo.save(admin);
		}
	}
	
	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		User user = userRepo.findUserByMail(name);
		if (user != null) {
			return createSpringUser(user);
		}
		return null;
	}
	
	private GrantedAuthority createAuthority(User user) {
        return new SimpleGrantedAuthority(user.getRole());
    }
	
	public User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth instanceof AnonymousAuthenticationToken){
            return null;
        }

        String email = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();

        return userRepo.findUserByMail(email);
	}
	
	public boolean signup(User user) {
		if (userRepo.findUserByMail(user.getMail()) == null) {
			user.setPw(passwordEncoder.encode(user.getPw()));
			userRepo.save(user);
			signin(user);
			return true;
		}
		return false;
	}
	
	public void signin(User user) {
		SecurityContextHolder.getContext().setAuthentication(authenticate(user));
	}
	
	private Authentication authenticate(User user) {
        return new UsernamePasswordAuthenticationToken(
        		createSpringUser(user),
        		null,
        		Collections.singleton(createAuthority(user)));
    }
	
	private org.springframework.security.core.userdetails.User createSpringUser(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getMail(),
                user.getPw(),
                Collections.singleton(createAuthority(user)));
    }
	
	public boolean changePw(changePwForm form) {
		String pw = form.getOldPw();
		String newPw = form.getNewPw();
		User user = form.getUser();
		if (pw == null || newPw == null || pw.isEmpty() || newPw.isEmpty())
            return false;
		user.setPw(passwordEncoder.encode(newPw));
		userRepo.save(user);
		return true;
	}
}
