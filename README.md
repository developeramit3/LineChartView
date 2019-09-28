# LineChartView
  
  
     allprojects {
	  	repositories {
		  	...
			  maven { url 'https://jitpack.io' }
		  }
	  }
    
    
    
     dependencies {
	        implementation 'com.github.developeramit3:LineChartView:e900ffd463'
	  }


[![](https://jitpack.io/v/developeramit3/LineChartView.svg)](https://jitpack.io/#developeramit3/LineChartView)


        LineChartView.Data data = new LineChartView.Data(value);
              datas.add(data);
	     lineChartView = (LineChartView)findViewById(R.id.line_chart_view);
	       lineChartView.setData(datas);
	        sb_ruler_space.setMax(70);
        sb_ruler_space.setProgress(20);
        if (lineChartView != null) {
            lineChartView.setRulerYSpace(20);
            tv_ruler_y.setText(String.valueOf(20));
        }
	
	 private boolean isBezier = false;

    public void bezierModelToggle(View view) {
        if (lineChartView != null) {
            isBezier = !isBezier;
            lineChartView.setBezierLine(isBezier);
        }
    }

    private boolean isCube = false;

    public void pointModelToggle(View view) {
        if (lineChartView != null) {
            isCube = !isCube;
            lineChartView.setCubePoint(isCube);
        }
    }

    public void doAnimation(View view) {
        if (lineChartView != null) {
            lineChartView.playAnim();
        }
    }
    
       <www.develpoeramit.linechartviewlib.LineChartView
                    android:id="@+id/line_chart_view"
                    android:layout_width="wrap_content"
                    android:layout_height="300dp"/>
		    
