package com.ticket_booking.service.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ticket_booking.entity.User;
import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.utils.oauth2.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class OAuth2UserService extends DefaultOAuth2UserService{
    
    private UserRepository userRepository;
    
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
        )   ;

            throw new OAuth2AuthenticationException(error, ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User){
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getName();

        log.info("Processing OAuth2 user: {} from provider: {}", email, registrationId);

        User user = userRepository.findByEmail(email)
                    .orElseGet(()->{
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setFullName(name);
                        newUser.setProfilePicture(picture);
                        newUser.setAuthProvider(AuthProvider.GOOGLE);
                        newUser.setProviderId(providerId);
                        newUser.setIsActive(true);

                        return userRepository.save(newUser);
                    });
        if(user.getProviderId() == null) {
            user.setProviderId(providerId);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user = userRepository.save(user);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
