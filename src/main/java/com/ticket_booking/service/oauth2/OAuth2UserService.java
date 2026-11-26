package com.ticket_booking.service.oauth2;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ticket_booking.entity.Role;
import com.ticket_booking.entity.User;
import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.enums.RoleCode;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.repository.RoleRepository;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.security.user.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class OAuth2UserService extends DefaultOAuth2UserService{
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Override 
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        
        try{

            OAuth2User oAuth2User = super.loadUser(userRequest);
            return processOAuth2User(userRequest, oAuth2User);

        } catch (OAuth2AuthenticationException ex){
            
            log.error("Error processing OAuth2 user: ", ex);
            
            OAuth2Error error = new OAuth2Error(
                "oauth2_user_processing_failed",
                "Unable to process OAuth2 user",
                null
            );

            throw new OAuth2AuthenticationException(error, ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User){
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getName();
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                "email_required",
                "Google không cung cấp email. Vui lòng cấp quyền email.",
                null
            ));
        }

        log.info("Processing OAuth2 user: {} from provider: {}", email, registrationId);
        
        // if (Boolean.FALSE.equals(emailVerified)) {
        //     throw new OAuth2AuthenticationException(new OAuth2Error(
        //         "email_unverified",
        //         "Email Google chưa được xác thực",
        //         null
        //     ));
        // }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        log.info("Processing OAuth2 user: {} from {}", normalizedEmail, registrationId);
        
        // User user = userRepository.findByEmail(email)
        //             .orElseGet(()-> {
        //                 Optional<Role> role = roleRepository.findByCode(RoleCode.USER);
        //                 User newUser = new User();
        //                 newUser.setEmail(email);
        //                 newUser.setFullName(name);
        //                 newUser.setProfilePicture(picture);
        //                 newUser.setAuthProvider(AuthProvider.GOOGLE);
        //                 newUser.setProviderId(providerId);
        //                 newUser.setActive(true);
        //                 newUser.setRole(role.get());
        //                 return userRepository.save(newUser);
        //             });
        // if(user.getProviderId() == null) {
        //     user.setProviderId(providerId);
        //     user.setAuthProvider(AuthProvider.GOOGLE);
        //     user = userRepository.save(user);
        // }

        User user = userRepository.findByEmail(normalizedEmail)
                    .map(existing -> handleExistingUser(existing, providerId, picture))
                    .orElseGet(() -> createNewGoogleUser(normalizedEmail, name, picture, providerId));

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }


    private User createNewGoogleUser(String email, String name, String picture, String providerId){
        Role role = roleRepository.findByCode(RoleCode.USER).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        User user = new User();
        user.setEmail(email);
        user.setFullName(name);
        user.setProfilePicture(picture);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setRole(role);
        User savedUser = userRepository.save(user);
        log.info("Created new google user: id{}, email={}");

        return savedUser;
    }

    private User handleExistingUser(User existingUser, String providerId, String picture){
        boolean needUpdate = false;

        if(existingUser.getProviderId() == null){
            existingUser.setProviderId(providerId);
            needUpdate = true;
            log.info("Linked providerId {} to existing user: id{}, email={}",providerId, existingUser.getEmail());
        }

        if (picture != null && !picture.equals(existingUser.getProfilePicture())) {
            existingUser.setProfilePicture(picture);
            needUpdate = true;
        }

        return needUpdate ? userRepository.save(existingUser) : existingUser;
    }
}
