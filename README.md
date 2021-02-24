# XRichEditorText
安卓富文本编辑类库, 支持图文混排, 需androidx
Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.GinGod:XRichEditorText:v1.0.0'
	}

Step 3. 混淆规则

    -keep class com.gingod.xricheditortextlib.bean.** {*;}
    -dontwarn com.gingod.xricheditortextlib.bean.**
