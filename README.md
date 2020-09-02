GitHub 에서 사용자를 검색하는 Test APP 입니다.

GitHub API v2 를 연동하고 있으며, JSON Restfull API 방식으로 자료를 주고받습니다.


검색키를 입력하면 검색 단어별로 즉시 검색이 되고, 아래 TAB 에 검색 결과를 출력합니다.

검색 결과에서 체크박스 선택되어진 항목은 다음 Local TAB으로 보관됩니다.

Local TAB 은 Local DB 를 이용하고 있으며, Local TAB 에 저장된 항목은 종료 되더라도 지워지지 않습니다.


API TAB 은 google open source paging2 를 활용한 무한 스크롤을 지원하고,

Local TAB 은 room 을 활용한 SQLite DB 를 처리합니다.

^^

[추가기능]
API TAB 에서 체크되어진 항목은 즉시 Local TAB 에 추가 반영 됨.

Local TAB 에서 체크해제 되어진 항목은 즉시 삭제되고, API TAB 에 즉시 반영 됨.

API TAB 에서 검색어 입력시 바로 검색이 이루어짐.

Local TAB 에 추가된 항목도 검색가능.


---
사용언어 : Kotlin

사용LIB : 

androidx.constraintlayout

androidx.paging

androidx.recyclerview

androidx.room

com.squareup.retrofit2

com.squareup.okhttp3

com.github.bumptech.glide

---

![sample1](https://github.com/nalgae/Project/blob/master/sample1.png)
![sample2](https://github.com/nalgae/Project/blob/master/sample2.png)