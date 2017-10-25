StoriesProgressView
====

show horizontal progress like instagram stories.

[![](https://jitpack.io/v/shts/StoriesProgressView.svg)](https://jitpack.io/#shts/StoriesProgressView)

<img src="image/capture.png" width=200 />

<img src="image/image.gif" width=200 /> 

^She is [Yui Kobayashi](http://www.keyakizaka46.com/s/k46o/artist/07)

How to Use
----

To see how the StoriesProgressView are added to your xml layouts, check the sample project.

```xml
    <jp.shts.android.storiesprogressview.StoriesProgressView
        android:id="@+id/stories"
        android:layout_width="match_parent"
        android:layout_height="3dp"
        android:layout_gravity="top"
        android:layout_marginTop="8dp" />
```
Overview

```java
public class YourActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {
    private StoriesProgressView storiesProgressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(PROGRESS_COUNT); // <- set stories
        storiesProgressView.setStoryDuration(1200L); // <- set a story duration
        storiesProgressView.setStoriesListener(this); // <- set listener
        storiesProgressView.startStories(); // <- start progress
    }

    @Override
    public void onNext() {
        Toast.makeText(this, "onNext", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPrev() {
        // Call when finished revserse animation.
        Toast.makeText(this, "onPrev", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "onComplete", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }
}
```

Skip and Reverse story
---

<img src="image/skip-reverse.gif" width=200 />

```java
  storiesProgressView.skip();
  storiesProgressView.reverse();
```

Pause and Resume story
---
<img src="image/pause-resume.gif" width=200 />

```java
  storiesProgressView.pause();
  storiesProgressView.resume();
```


Install
---

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
}

```

Add the dependency

```
	dependencies {
	        compile 'com.github.shts:StoriesProgressView:2.0.0'
	}

```

License
---

```
Copyright (C) 2017 Shota Saito(shts)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
