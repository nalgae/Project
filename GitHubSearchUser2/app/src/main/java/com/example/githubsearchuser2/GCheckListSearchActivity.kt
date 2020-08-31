package com.example.githubsearchuser2

import android.app.DatePickerDialog
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.multidex.MultiDex
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import java.util.*


class GCheckListSearchActivity : AppCompatActivity() {
    private var mToolbar: Toolbar? = null
    private var mTabLayout: TabLayout? = null
    lateinit var mViewPager: ViewPager
    private var strSearchUser = ""
    private var iSearchType = 0 // 0: API, 1: Local
    private var strAutoRun = ""
    private var strQueryText = ""


    private var mTwoAdapterHeaderList: MutableList<GCheckListSearchHeaderRow>? = null

    data class GCheckListSearchHeaderRow(var tHeaderIDX: String, var tHeaderType: String, var tHeaderName: String, var tHeaderBody: String, var tHeaderBody2: String
                                         , var tHeaderBodyStatus: String, var tHeaderJobDT: String)

    companion object {
        lateinit var mContext: Context
        var mSnackbarLayout: View? = null
        lateinit var eventTypesDB: EventTypesDao
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    // 외부호출 검색
    private val mGCheckListSearchAutoRunReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            GCheckListSearchFragment2.GCallLoadTimeLineHeaderList(0, 0, strQueryText)
        }
    }

    override fun onCreate(savedBundle: Bundle?) {
        super.onCreate(savedBundle)

        mContext = this
        eventTypesDB = EventsDatabase.getInstance(mContext).EventTypesDao()

        setContentView(R.layout.gchecklist_activity_search)
        mSnackbarLayout = findViewById(android.R.id.content) // 아무 View 레이아웃이어도 된다.
        registerReceiver(mGCheckListSearchAutoRunReceiver, IntentFilter(NexBroadcastReceiver.CHECKLIST_SEARCH_AUTORUN_REFRESH))

        // 최상단 Toolbar 에 대한 선언
        mToolbar = findViewById(R.id.search_toolbar)
        setSupportActionBar(mToolbar)
        //Toolbar의 왼쪽에 버튼을 추가하고 버튼의 아이콘을 바꾼다.
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu) // Toolbar 왼쪽에 버튼 추가
        supportActionBar!!.title = "검색"

        // TAB View 에 대한 선언
        mViewPager = findViewById(R.id.search_viewpager)
        setupViewPager(mViewPager)
        mTabLayout = findViewById(R.id.search_tabs)
        mTabLayout!!.setupWithViewPager(mViewPager)

        // TAB 선택위치 확인해서 처리하기 ----------------------------------------------------
        mTabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                    val position = tab.position
                    if (position == 0) {
                        iSearchType = 0 // API
                    } else {
                        iSearchType = 1 // Local
                        queryTextListener.onQueryTextSubmit("")
                    }
                    // 환경설정에 현재 TAB 위치 기록
                    //SharedPrefSetting(mContext).SearchTabNumber = iSearchType
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        // ---------------------------------------------------------------------------------
        // 외부호출 검색
        // bundle ---------------------------------------------
        val newIntent = intent
        if (newIntent.hasExtra("AutoRun")) strAutoRun = newIntent.getStringExtra("AutoRun")
        if (newIntent.hasExtra("QueryText")) strQueryText = newIntent.getStringExtra("QueryText")
        if (!strAutoRun.isEmpty() && strAutoRun == "1") {
            val intent = Intent(NexBroadcastReceiver.CHECKLIST_SEARCH_AUTORUN_REFRESH)
            mContext.sendBroadcast(intent)
        }
        // -----------------------------------------------------
    }


    // ToolBar // ToolBar에 menu.xml을 인플레이트함
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        // AppBar 추가
        menuInflater.inflate(R.menu.toolbar_appbar_search, menu)

        // 검색 추가 -------------------------------------------------------------------------------
        // 주의*) 검색버튼을 Toolbar에 추가했기 때문에 클릭이벤트를 onOptionsItemSelected 메소드에서 처리했었는데
        // 액션바의 검색기능을 살리려면 onCreateOptionsMenu 메소드에서 이벤트를 다루어야 한다는 점 !!!
        // 검색 버튼 클릭했을 때 searchView 길이 꽉차게 늘려주기
        val searchView = MenuItemCompat.getActionView(menu.findItem(R.id.action_search)) as SearchView
        searchView.maxWidth = Integer.MAX_VALUE

        // 검색 버튼 클릭했을 때 searchView 에 힌트 추가
        searchView.queryHint = "GitHub user search"
        if (strQueryText.isNotEmpty()) searchView.queryHint = strQueryText // 외부호출 검색일 경우 검색어 화면에 표시

        // 리스너 구현
        searchView.setOnQueryTextListener(queryTextListener)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false) // 검색창의 기본상태 설정, true : 필드가 보임, false : 아이콘으로 보임
        if (strAutoRun != "1") { // 외부호출 검색일 경우 포커스를 입력상태로 만들지 않는다.
            searchView.requestFocusFromTouch() // 처음 실행시 포커스가 입력상태로 되게 설정
        }
        // -----------------------------------------------------------------------------------------

        return true // 반드시 true를 리턴. false를 리턴하면, 메뉴가 표시되지 않는다.
    }

    // ToolBar // 추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
/*            android.R.id.home -> {
                finish()
                return true
            }*/
            R.id.action_search -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // 검색을 위한 Query Text Listener 정의
    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(mSearchUser: String?): Boolean {
            strSearchUser = mSearchUser!!
            if (!strSearchUser.isEmpty()) {
                strSearchUser = strSearchUser.replace("\r", "").replace("\n", "")
                if( iSearchType == 0 ) {
                    GCheckListSearchFragment2.GCallLoadTimeLineHeaderList(0, 0, strSearchUser)
                } else {
                    GCheckListSearchTextFragment.LoadCheckListHeaderList(strSearchUser)
                }
            }
            return false
        }

        override fun onQueryTextChange(s: String): Boolean {
            strSearchUser = s

            if (strSearchUser != null) {
                strSearchUser = strSearchUser.replace("\r", "").replace("\n", "")
                if( iSearchType == 0 ) {
                    GCheckListSearchFragment2.GCallLoadTimeLineHeaderList(0, 0, strSearchUser)
                } else {
                    GCheckListSearchTextFragment.LoadCheckListHeaderList(strSearchUser)
                }
            }

            return false
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GCheckListSearchFragment2(), "API")
        adapter.addFragment(GCheckListSearchTextFragment(), "Local")
        viewPager.adapter = adapter

        // 시작시 원하는 viewpage 로 이동하기 ------------------------------------------
//11        viewPager.setCurrentItem( SharedPrefSetting(mContext).SearchTabNumber ) // 환경설정에 현재 TAB 위치 가져옴
        mTabLayout?.setupWithViewPager(viewPager)
//11        if( SharedPrefSetting(mContext).SearchTabNumber == 0 ) iSearchType = 0 // 문자검색
//11        else iSearchType = 1 // 이미지검색
        // --------------------------------------------------------------------------------------
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    // 외부호출 검색
    override fun onDestroy() {
        super.onDestroy()

        EventsDatabase.destroyInstance()
        unregisterReceiver(mGCheckListSearchAutoRunReceiver)
    }


    fun doCheckListSearchText(_strSearchUser: String) {
        //GCheckListSearchTextFragment.LoadGCheckListSearchHeaderList()

        queryTextListener.onQueryTextSubmit(_strSearchUser)
    }


}