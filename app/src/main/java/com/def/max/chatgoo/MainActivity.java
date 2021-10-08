package com.def.max.chatgoo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    static
    {
        System.loadLibrary("native-lib");
    }

    private DatabaseReference userRef;

    private String onlineUserId;

    private InterstitialAd mInterstitialAd;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            onlineUserId = firebaseUser.getUid();
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        final CircleImageView currentImageCircleImageView = findViewById(R.id.current_image_circle_image_view);

        int[] icons = {R.drawable.ic_bot, R.drawable.ic_friends,};

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.main_tab_content);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < icons.length; i++)
        {
            tabLayout.getTabAt(i).setIcon(icons[i]);
        }

        tabLayout.getTabAt(0).select();

        if (onlineUserId != null)
        {
            userRef.child(onlineUserId).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.hasChild("user_profile_thumb_img"))
                    {
                        final String image = dataSnapshot.child("user_profile_thumb_img").getValue().toString();

                        if (!image.equals("default_profile_thumb_img"))
                        {
                            Picasso.with(MainActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(currentImageCircleImageView, new Callback()
                            {
                                @Override
                                public void onSuccess()
                                {

                                }

                                @Override
                                public void onError()
                                {
                                    Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.user_icon).into(currentImageCircleImageView);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Crashlytics.log(databaseError.getMessage());
                }
            });

        }

        MobileAds.initialize(this, "ca-app-pub-4443035718642364~2425994198");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4443035718642364/2106429990");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (mInterstitialAd.isLoaded())
                {
                    mInterstitialAd.show();
                }
                else
                {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }

                handler.postDelayed(this, 300 * 1000);
            }
        }, 3000);

        final AdView mAdView = findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        final Handler handlerBanner = new Handler();
        handlerBanner.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                AdRequest adRequest2 = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest2);

                handlerBanner.postDelayed(this, 300 * 1000);
            }
        }, 1000);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.insertNewFragment(new AssistantFragment());
        adapter.insertNewFragment(new ChatFragment());
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager)
        {
            super(manager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }

        void insertNewFragment(Fragment fragment)
        {
            mFragmentList.add(fragment);
        }
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null)
        {
            Intent mainIntent = new Intent(MainActivity.this,WelcomeActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        }
    }
}