package com.example.materialtest;

import android.inputmethodservice.Keyboard;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Fruit[] fruits = {new Fruit("apple", R.drawable.apple), new Fruit("banana", R.drawable.banana), new Fruit("orange", R.drawable.banana),
            new Fruit("watermelo", R.drawable.watermelon), new Fruit("pear", R.drawable.pear), new Fruit("grape", R.drawable.grape),
            new Fruit("pineapple", R.drawable.pineapple), new Fruit("strawberry", R.drawable.strawberry), new Fruit("cherry", R.drawable.cherry),
            new Fruit("mange", R.drawable.mango)};
    private List<Fruit> fruitList = new ArrayList<>();
    private FruitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //由于actionbar的功能相对简单，因此用toolbar来代替，在全局设置为noActionBar的主题
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//必须设置这一步，让toolbar作为actionbar的替代者
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);//设置是否显示HomeAsUp按钮，就是最左边的按钮，默认图标是向左的箭头
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);//更改为自己的图标
        }
        //在onCreateOptionsMenu中去创建actionbar上的菜单
        //在onOptionsItemSelected上去处理菜单的点击事件

        //FloatingActionButton用意是为重要而频繁功能提供一个快捷操作入口，本质就是一个按钮
        //它会漂浮在内容上面，遮挡下面的内容，因此会有阴影效果
        //设置它的点击事件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                可以新建Snackbar的实例，然后获取它的view对象，就可以设置其他属性，如背景颜色
//                snackbar.getView().setBackgroundColor(colorId);
                //使用snackbar时注意，如果有可能处于输入状态，要先把键盘隐藏掉，不然会被键盘遮挡住
                //还有，SnackBar的实现方式其实是addView，因此，它的父控件不能是ScrollView这样只允许有一个子控件的控件。
                //使用技巧，让触发的这个view的父控件是CoordinatorLayout就可以了，并支持右划取消
                Snackbar.make(v, "Data deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "Data restored", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

//        设置DrawerLayout中的滑出部分，在这里放了一个NavigationView导航，这个导航可以说是专为DrawerLayout而设计的
//        NavigationView分为两部分，一个是头部，一个是导航菜单，在布局文件中可以设置指定
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_call);//设置默认选中的项
//        设置菜单选择监听器，暂时没有具体跳转，只是让他们把DrawerLayout的滑出部分隐藏
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_call:
                    case R.id.nav_friends:
                    case R.id.nav_mail:
                    case R.id.nav_location:
                    case R.id.nav_task:
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    default:
                }
                return true;
            }
        });

        //随机复制水果的数据，做成50个数据
        initFruits();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new FruitAdapter(fruitList);
        recyclerView.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置刷新转动条的颜色
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFruits();
            }
        });
    }

    private void refreshFruits() {
        //这里只是为了模拟刷新的过程，因为数据在本地，刷新会非常快，看不出效果，
        //因此启用子线程，让线程沉睡2秒钟，然后再回到主线程重新生成数据，并通知recyclerview刷新数据
        //然后停止刷新状态，隐藏刷新标志
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initFruits();
                        adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void initFruits() {
        fruitList.clear();
        Random random;
        int index;
        for (int i = 0; i < 50; i++) {
            random = new Random();
            index = random.nextInt(fruits.length);
            fruitList.add(fruits[index]);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.backup:
                Toast.makeText(this, "you click backup", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "you click delete", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Toast.makeText(this, "you click settings", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }
}
