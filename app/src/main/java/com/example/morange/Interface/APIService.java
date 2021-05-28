package com.example.morange.Interface;

import com.example.morange.Notifications.MyResponse;
import com.example.morange.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAgrjyBfc:APA91bFPdZz-qxA3IX-p0sdEQA7WfFEAq-jBscgY7rlwctMXKtn0ga3vytKUjG6hMrGOxGoL9IHBeDsQ1tv0Ud3pUkTYqC10HRiKOlRcI5LtYuObmVQyjScFCCM1ELyQ3mZEbe-nGwLv"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);


}
