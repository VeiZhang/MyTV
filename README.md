# MyTV

### 电视端相关的一些常用API集合

#### M3U解析

* 规范
    
    https://tools.ietf.org/html/rfc8216

* 解析速度

    Amlogic box运行内存1G，10M文件解析速度为2s
    
* 解析准确率

    ```
    1.可以解析带多个逗号的行：
    #EXTINF:-1 tvg-id="TV 8,5 TR" tvg-name="||TR|| TV 8.5 HD" tvg-logo="http://tv.trexiptv.com:8000/picons/logos/tv8.png" group-title="TURKEY I ULUSAL",||TR|| TV 8.5 HD
    ```