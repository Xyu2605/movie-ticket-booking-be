package com.ticket_booking.utils.ratelimit;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties {
    
    private Map<String, Rule> rules;
    
    public Map<String, Rule> getRule(){
        return rules;
    }

    public void setRules(Map<String, Rule> rules){
        this.rules = rules;
    }

    public static class Rule{
        
        private long capacity; //Sức chứa tối đa của bucket, tức số token tối đa
        private long refillToken; //Số token được nạp lại mỗi chu kỳ
        private long refillPeriodSeconds; // Thời gian của 1 chu kì nạp token

        public long getCapacity(){
            return capacity;
        }

        public long getRefillToken(){
            return refillToken;
        }

        public long getRefillPeriodSeconds(){
            return refillPeriodSeconds;
        }

        public void setCapacity(long capacity){
            this.capacity = capacity;
        }

        public void setRefillToken(long refillToken){
            this.refillToken = refillToken;
        }

        public void setRefillPeriodSeconds(long refillPeriodSeconds){
            this.refillPeriodSeconds = refillPeriodSeconds;
        }
    }
}
