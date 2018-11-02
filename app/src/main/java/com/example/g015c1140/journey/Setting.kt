package com.example.g015c1140.journey

class Setting {

    val IP_ADDRESS = "192.168.43.84:3000"
    val SERVER_ADDRESS = "35.200.26.70:443"
    val USER_ID = "minomino114"

    val USER_GET_URL = "http://$IP_ADDRESS/api/v1/users/find"
    val USER_POST_URL = "http://$IP_ADDRESS/api/v1/users/register"
    val SPOT_IMAGE_POST_URL = "http://$IP_ADDRESS/api/v1/image/upload"
    val SPOT_POST_URL = "http://$IP_ADDRESS/api/v1/spot/register/"
    val SPOT_GET_URL = "http://$IP_ADDRESS/api/v1/spot/find?user_id=$USER_ID"
    val PLAN_POST_URL = "http://$IP_ADDRESS/api/v1/plan/register/"

}