# FormatEditText
## [English](README_EN.md) | 中文

 **本库提供3个类以供使用:**
 - `ClearEditText` 可以用来当带清除功能的文本输入框使用.
 - `FormattedEditText` 可以用来当做号码格式化文本输入框使用.
 - `MaskNumberEditText` 可以用来当做数字或金额文本输入框使用.

## 特性:
 - 支持配置格式化样式
 - 支持配置输入提示
 - 支持粘贴且光标自动跟随
 - 自动填充删除占位符
 - 支持配置清除图标且不会占用CompoundDrawables的位置

## 演示程序
下载 [Demo.apk](https://github.com/dkzwm/FormatEditText/raw/develop/apk/demo.apk)
## 快照
<img src='snapshot.gif'></img>
## 引入
添加如下依赖到你的 build.gradle 文件:
```
dependencies {
    implementation 'me.dkzwm.widget.fet:core:0.2.0'
}
```
## 使用
#### 在Xml中配置
```
<me.dkzwm.widget.fet.FormattedEditText
    android:id="@+id/formattedEditText_simple"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="13"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="344"
    app:fet_mode="mode_simple"
    app:fet_placeholder="-" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="19"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="+(86)-***-****-****"
    app:fet_mark="*"
    app:fet_mode="mode_complex" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:inputType="phone"
    android:maxLength="19"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_emptyPlaceholder="_"
    app:fet_formatStyle="+(86)-000-0000-0000"
    app:fet_mode="mode_mask"
    app:fet_showHintWhileEmpty="true" />

<me.dkzwm.widget.fet.FormattedEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:digits="0123456789X"
    android:maxLength="22"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_formatStyle="000 000 0000 0000 000*"
    app:fet_hintText="100 000 2020 0101 000X"
    app:fet_mode="mode_hint"
    app:fet_showHintWhileEmpty="false" />

<me.dkzwm.widget.fet.MaskNumberEditText
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:fet_clearDrawable="@drawable/icon_clear"
    app:fet_drawableGravity="fet_center"
    app:fet_drawablePadding="4dp"
    app:fet_autoFillNumbers="false"
    app:fet_currencySymbol="￥"
    app:fet_decimalLength="2"
    app:fet_showThousandsSeparator="true" />
```
####  Java代码配置
```
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_SIMPLE) //简单模式
        .formatStyle("344") //格式化样式
        .placeholder(' ') //占位符
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_COMPLEX) //组合模式
        .formatStyle("+(86)-***-****-****") //格式化样式
        .mark('*') //标记符
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_MASK) //掩码匹配模式
        .formatStyle("+(86)-000-0000-0000") //格式化样式
        .emptyPlaceholder('_') //空白数据占位符
        .showHintWhileEmpty(true) //是否清除数据后显示默认提示文字（android:hint），开启后不会用空白数据占位符填充数据位
        .config(editText);
FormattedEditText.Config.create()
        .mode(FormattedEditText.MODE_HINT) //提示模式
        .formatStyle("000 000 0000 0000 000X") //格式化样式
        .maskFilter("X", new FormattedEditText.Matcher() {
            @Override
            public boolean hasMatch(String previousText, String value) {
                return TextUtils.isDigitsOnly(value) || value.toUpperCase().equals("X");
            }
        })//自定义掩码匹配
        .maskFilter("0", new FormattedEditText.Matcher() {
            @Override
            public boolean hasMatch(String previousText, String value) {
                return TextUtils.isDigitsOnly(value);
            }
        })//自定义掩码匹配
        .hintText("100 000 2020 0101 000X") //提示文字，不同于默认提示文字（android:hint），会在输入字符后依然显示，必须和格式化样式格式一致
        .hintColor(Color.GRAY) //提示文字颜色
        .showHintWhileEmpty(true) //是否清除数据后显示默认提示文字（android:hint），开启后不会用空白数据占位符填充数据位
        .config(editText);
MaskNumberEditText editText = new MaskNumberEditText(context);
editText.setShowThousandsSeparator(true);//是否显示千位分隔符`,`
editText.setAutoFillNumbers(true);//是否自动填充小数，如设置小数保留2位，那么当位数不足时会自动填充`0`
editText.setDecimalLength(2);//小数位长度
editText.setCurrencySymbol("￥");//设置货币符号，不设置即不显示
```
#### Xml属性
##### ClearEditText
|名称|类型|描述|
|:---:|:---:|:---:|
|fet_clearDrawable|reference|指定删除图标|
|fet_drawableGravity|enum|指定删除图标的对齐方式，支持`GRAVITY_TOP`、`GRAVITY_CENTER`、`GRAVITY_BOTTOM`，默认为`GRAVITY_CENTER`，即居中对齐|
|fet_drawablePadding|dimension|指定删除图标的填充大小|

##### FormattedEditText
|名称|类型|描述|
|:---:|:---:|:---:|
|fet_mode|enum|指定模式，支持`MODE_SIMPLE`（简单模式）、`MODE_COMPLEX`(组合模式)、`MODE_MASK`(掩码匹配模式)、`MODE_HINT`(提示模式)|
|fet_formatStyle|string|指定格式化样式，当`fet_mode`为`MODE_SIMPLE`时，格式只能是纯数字, `fet_mode`为`MODE_COMPLEX`时，格式为任意格式且需要指定`fet_mark`属性，如果不指定那么默认为`*`|
|fet_mark|string|指定标记符，仅当`fet_mode`为`MODE_COMPLEX`时设置会起作用，且长度必须为1（默认:`*`）|
|fet_placeholder|string|指定占位符，仅当`fet_mode`为`MODE_SIMPLE`时设置会起作用，且长度必须为1（默认:` `）|
|fet_emptyPlaceholder|string|指定空白占位符，仅当`fet_mode`为`MODE_MASK`时设置会起作用，且长度必须为1（默认:` `）|
|fet_hintText|string|指定提示文字，仅当`fet_mode`为`MODE_HINT`时设置会起作用，必须和格式化样式格式一致|
|fet_hintTextColor|color|指定提示文字颜色，仅当`fet_mode`为`MODE_HINT`时设置会起作用|
|fet_showHintWhileEmpty|boolean|指定是否清除数据后显示默认提示文字（android:hint），仅当`fet_mode`为`MODE_MASK`和`MODE_HINT`时设置会起作用|

##### MaskNumberEditText
|名称|类型|描述|
|:---:|:---:|:---:|
|fet_decimalLength|integer|指定小数位长度|
|fet_currencySymbol|string|指定货币符号|
|fet_currencySymbolTextColor|string|指定货币符号文字颜色，不设置的话默认使用当前文字颜色|
|fet_showThousandsSeparator|boolean|指定是否显示千位分隔符|
|fet_autoFillNumbers|boolean|指定是否自动填充数字|
|fet_autoFillNumbersTextColor|boolean|指定自动填充数字的文字颜色，不设置的话默认使用当前提示文字颜色|
#### 掩码
`FormattedEditText` 在模式为`MODE_MASK`和`MODE_HINT`时，格式化样式中的以下字符具有特殊含义：

 - 0 \- 数字掩码，只接受输入数字
 - A \- 英文字母掩码，只接受输入英文字母
 - \* \- 数字和英文字母掩码，接受输入数字和英文字母
 - ? \- 字符掩码，接受输入任何内容
 
样式中的其他字符则会原样显示。如果需要原样显示这4个特殊字符，则需要使用转义符`\`。例如`\\0\\086 000 000 000`在格式化时`0086`会原样显示。    

## 感谢
- [reinaldoarrosi—MaskedEditText](https://github.com/reinaldoarrosi/MaskedEditText)   

## License
	--------

    	Copyright (c) 2017 dkzwm

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.
