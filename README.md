# Road-Abnormal-Detection

道路异常检测应用

**目的**
根据共享单车等的行进路线信息来检测道路是否存在异常情况

## Week 1:

### Task：采集GPS及WiFi传感器读数

**GPS读数部分：**

![GPS](https://i.loli.net/2019/03/02/5c7a7c34748c1.png)


  说明：

1. 运行程序时需要打开GPS，不然会出错。
2. 运行在 Android 24及以上版本。
3. 当GPS信号不好时，数据不会自动更新。
4. 为了避免产生占用过多内存的麻烦，暂时没有加入自动保存数据的功能。


**WIFI读数部分：**

![Screenshot_20190304_125157_com.example.peanut.gps](media/Screenshot_20190304_125157_com.example.peanut.gps.jpg)


  说明：
  
1. WIFI信号数量较多，暂时只取强度较好的5个wifi记录。
2. wifi实现每秒更新，但由于安卓的wifi并没有每秒扫描，所以要过一段时间才会更新一次
3. wifi强度是以负数的形式存在，值越接近0，表示信号越好。