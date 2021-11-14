package com.msoul.simpleyelp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URl = "https://api.yelp.com/v3/"
private const val TAG = "MainActivity"
private const val API_KEY = "TKzqI3YDF2Mbk7TnHPhuCiSST4s3TxbFQWAY-Nx2erwoan6oRYQ2far1PKVLuW5OabF_9zxe2D6fqnXVV-aBlchZQjwUngt9MQVJpHuu5JCJ1rwE_v3p8Jzo0IqHYXYx"
private lateinit var rvRestaurants: RecyclerView
private lateinit var etQuery: EditText
private lateinit var searchButton: ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvRestaurants = findViewById(R.id.rvRestaurants)
        etQuery = findViewById(R.id.etQuery)
        searchButton = findViewById(R.id.searchButton)

        val restaurants = mutableListOf<YelpRestaurant>()
        val adapter = RestaurantsAdapter(this, restaurants)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)


        val retrofit = Retrofit.Builder().baseUrl(BASE_URl).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)

        searchButton.setOnClickListener {
            val curQuery = etQuery.text
            val searchArray = curQuery.split(",").toTypedArray()
            if(searchArray.size != 2) {
                Toast.makeText(this, "Please include a search term and location separated by a comma", Toast.LENGTH_LONG)
                return@setOnClickListener
            }
            restaurants.clear()
            adapter.notifyDataSetChanged()
            yelpService.searchRestaurants( "Bearer $API_KEY",searchArray[0], searchArray[1]).enqueue(object: Callback<YelpSearchResults> {
                override fun onResponse(call: Call<YelpSearchResults>, response: Response<YelpSearchResults>) {
                    Log.i(TAG, "onResponse $response")
                    val body = response.body()
                    if(body == null) {
                        Log.w(TAG, "Did not receive valid response body from Yelp API... exiting")
                        return
                    }
                    restaurants.addAll(body.restaurants)
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(call: Call<YelpSearchResults>, t: Throwable) {
                    Log.i(TAG, "onFailure $t")
                }
            })
        }
    }
}