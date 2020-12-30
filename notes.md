#### 2020-12-22
找到toolbar上加载MENU设置ICON颜色的方法了。

menu里,需要设置app:iconTint，设置成android:iconTint没用。。。。。。

代码里menu[i].icon.setTint(Color)就生效了

状态栏还要再看看。toolbar上返回键和overFlow图标要再看看，不过这个应该好找。

#### 2020-12-28
```kotlin
ReadBookConfig.bgMeanColor
ReadBookConfig.textColor
requireContext().getPrimaryTextColor(isLight)
ColorUtils.withAlpha( Color.parseColor("#292323"),0.95f)
context.getPrimaryTextColor(ColorUtils.isColorLight(bgColor))
```
#### 2020-12-29
```kotlin
val isLight = ColorUtils.isColorLight(bg)
ColorUtils.withAlpha( Color.parseColor("#292323"),0.95f)
ColorUtils.shiftColor(ReadBookConfig.bgMeanColor, 1.02f)

```

```
git pull git://github.com/gedoor/legado.git master
git add //conflict file
git commit -m "resolve the conflict"
git push origin master
git push -f origin master
```
#### 2020-12-30
```kotlin
colorControlNormal
val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
```
```
src\main\java\io\legado\app\utils\MenuExtensions.kt//设置图标颜色
src\main\java\io\legado\app\constant\AppConst.kt//设置全局阴影
```
