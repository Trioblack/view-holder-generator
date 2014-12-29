# view holder generator for Android


Android dev is painful. for example, `findViewById()`. some project like [butterknife](http://jakewharton.github.io/butterknife/) try to solve it by view injection. but is this really a good solution? the ideal way is for one view, only give it a name once. but using view injection, you still need to write the name three times:

```java
@InjectView(R.id.subtitle) TextView subtitle;
```

```xml
android:id="@+id/subtitle"
```

an alternative approach is use a uniform binding pattern for Java field names and view ids. using this tool, a layout file like this:

   ```xml
   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
       holder="main.MainAct"
       android:id="@+id/nav">

       <LinearLayout
           android:id="@+id/profile"
           style="@style/nav_item">

           <ImageView
               style="@style/image"
               android:id="@+id/image">

           <TextView
               style="@style/main_nav_left_item_text"/>
       </LinearLayout>
   ```

will generate code in the `MainAct` class which is specified in the xml `holder` attribute:

   ```Java
    LinearLayout _nav;
    LinearLayout _nav_profile;
    ImageView _nav_profile_image;

    private void __find_views_main_nav_left(View view) {
        _nav = ((LinearLayout)  view);
        _nav_profile = ((LinearLayout) ((ViewGroup) view).getChildAt(0));
        _nav_profile_image = ((VRounded) ((ViewGroup)((ViewGroup) view).getChildAt(0)).getChildAt(0));
    }

    protected View __inflate_view_main_nav_left(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(com.p1.mobile.putong.R.layout.main_nav_left, parent, false);
        __find_views_main_nav_left(view);
        return view;
    }
   ```

then you can use the `__inflate_view_` methods whatever way you want


## just a demo

it should be easy to support `include` and `merge` tags, but it is not done yet



