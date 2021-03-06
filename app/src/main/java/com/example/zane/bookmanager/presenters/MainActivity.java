package com.example.zane.bookmanager.presenters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.zane.bookmanager.R;
import com.example.zane.bookmanager.app.MyApplication;
import com.example.zane.bookmanager.inject.component.ActivityComponent;
import com.example.zane.bookmanager.inject.component.DaggerActivityComponent;
import com.example.zane.bookmanager.inject.module.ActivityModule;
import com.example.zane.bookmanager.model.bean.Book;
import com.example.zane.bookmanager.model.data.DataManager;
import com.example.zane.bookmanager.presenters.activity.BookInfoActivity;
import com.example.zane.bookmanager.presenters.fragment.MyBookInfoFragment;
import com.example.zane.bookmanager.utils.JudgeNetError;
import com.example.zane.bookmanager.view.MainView;
import com.example.zane.easymvp.presenter.BaseActivityPresenter;
import com.kermit.exutils.utils.ExUtils;

import javax.inject.Inject;

import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivityPresenter<MainView> {

    public static final int requestCode_1 = 1001;
    public static final int requestCode_2 = 1002;
    public static final String ISBN = "ISBN";
    public static final String BOOK_INFO = "BOOKINFO";
    public static final String TAG = "MainActivity";
    private String isbn;
    private MyBookInfoFragment myBookInfoFragment;
    private ActivityComponent activityComponent;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    DataManager datamanager;

    @Override
    public Class<MainView> getRootViewClass() {
        return MainView.class;
    }

    @Override
    public void inCreat(Bundle bundle) {
        myBookInfoFragment = MyBookInfoFragment.newInstance();
        v.init(this, myBookInfoFragment);
        initInject();
        swipeRefreshLayout = v.get(R.id.swiperefreshlayout_main_activity);
        swipeRefreshLayout.setBackgroundResource(R.color.white);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setDistanceToTriggerSync(ExUtils.getScreenHeight());

    }

    public void initInject(){
        MyApplication app = MyApplication.getApplicationContext2();
        activityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule(this))
                .applicationComponent(app.getAppComponent())
                .build();
        activityComponent.inject(this);
    }

    public ActivityComponent getActivityComponent(){
        return activityComponent;
    }

    public void fetchData(String isbn){
        datamanager.getBookInfo(isbn)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Book>() {
                    @Override
                    public void onCompleted() {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        JudgeNetError.judgeWhitchNetError(e);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "aaa", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Book book) {
                        swipeRefreshLayout.setRefreshing(false);
                        Intent intent = new Intent(MainActivity.this, BookInfoActivity.class);
                        intent.putExtra(BOOK_INFO, book);
                        startActivity(intent);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            switch (requestCode) {
                case requestCode_1:
                    isbn = data.getStringExtra(ISBN);
                    swipeRefreshLayout.setRefreshing(true);
                    fetchData(isbn);
                    break;
                case requestCode_2:

                    break;
            }
        }else {
            return;
        }
    }

    @Override
    public void inDestory() {
    }

    @Override
    public AppCompatActivity getContext() {
        return this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

}
