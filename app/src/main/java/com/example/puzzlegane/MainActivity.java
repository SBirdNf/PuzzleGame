package com.example.puzzlegane;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private boolean isAnimRun=false;//当前动画是否在执行

    private boolean isGameStart=false;//判断游戏是否开始

    private ImageView[][] iv_game_arr = new ImageView[3][5];//利用二维数组创建若干拼图块3*5

    private GridLayout main_game;//游戏主界面

    private ImageView iv_null_ImageView;//当前空方块的实例保存

    private GestureDetector mDetector;//当前手势

    @Override
    public boolean onTouchEvent(MotionEvent event){//非图片位置可以进行手势滑动
        return mDetector.onTouchEvent(event);//手势监听
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){//图片上可以进行手势滑动
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
                int type = getDirByGes(e1.getX(),e1.getY(),e2.getX(),e2.getY());
                changeByDir(type);
                return false;
            }
        });
        setContentView(R.layout.activity_main);
        //初始化游戏的若干拼图块
        Bitmap bigBm=((BitmapDrawable)getResources().getDrawable(R.drawable.puzzle_bg)).getBitmap();
        int everyWidth = bigBm.getWidth()/5;//每个拼图的高和宽
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                Bitmap bm = Bitmap.createBitmap(bigBm,j*everyWidth,i*everyWidth,everyWidth,everyWidth);//根据行列来分割成若干个拼图块
                iv_game_arr[i][j] = new ImageView(this);
                iv_game_arr[i][j].setImageBitmap(bm);//设置每一块拼图的图案
                iv_game_arr[i][j].setPadding(2,2,2,2);//设置方块之间的间距
                iv_game_arr[i][j].setTag(new GameData(i,j,bm));//绑定自定义的数据
                iv_game_arr[i][j].setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        boolean flag = isHasByNullImageView((ImageView)view);
                        if (flag){
                            changeDataByImageView((ImageView)view);
                        }
                    }
                });
            }
        }
        //初始化游戏主界面，并添加若干个拼图块
        main_game = (GridLayout)findViewById(R.id.main_game);
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                main_game.addView(iv_game_arr[i][j]);
            }
        }
        setNullImageView(iv_game_arr[2][4]);//设置最后一个方块为空
        randomMove();//初始化随机打乱顺序
        isGameStart=true;//设置游戏开始状态
    }

    public void changeByDir(int type){
        changeByDir(type,true);
    }

    //根据手势的方向，获取空方块相应的临接位置如果存在方块，则进行数据交换
    //@param type 1:上 2：下 3：左 4：右
    //@param isAnim true：有动画 false：无动画
    public void changeByDir(int type,boolean isAnim){
        GameData mNullGameData = (GameData) iv_null_ImageView.getTag();
        int new_x=mNullGameData.x;
        int new_y=mNullGameData.y;
        if (type==1){//要移动的方块在当前空方快的下方（上）
            new_x++;
        }else if (type==2){//要移动的方块在当前空方快的上方（下）
            new_x--;
        }else if (type==3){//要移动的方块在当前空方快的右方（左）
            new_y++;
        }else if (type==4){//要移动的方块在当前空方快的左方（右）
            new_y--;
        }
        //判断这个新坐标是否存在
        if (new_x>=0&&new_x<iv_game_arr.length&&new_y>=0&&new_y<iv_game_arr[0].length){
            if (isAnim){
                //存在的话，开始移动
                changeDataByImageView(iv_game_arr[new_x][new_y]);
            }else{
                changeDataByImageView(iv_game_arr[new_x][new_y],isAnim);
            }
        }else{
            //什么也不做
        }
    }

    //判定游戏结束的方法
    public void isGameOver(){
        boolean isGameOver=true;
        //遍历每个游戏方块
        for (int i=0;i<iv_game_arr.length;i++){
            for (int j=0;j<iv_game_arr[0].length;j++){
                //为空的方块数据不判断跳过
                if (iv_game_arr[i][j]==iv_null_ImageView){
                    continue;
                }
                GameData mGameData = (GameData) iv_game_arr[i][j].getTag();
                if (!mGameData.isTrue()){
                    isGameOver=false;
                    break;
                }
            }
        }
        //根据一个开关变量决定游戏是否结束，结束是给出提示
        if (isGameOver){
            Toast.makeText(this,"游戏结束",Toast.LENGTH_LONG).show();
        }
    }
    //手势判断
    //@param start_x手势的起始点x
    //@param start_y手势的起始点y
    //@param end_x手势的结束点x
    //@param end_y手势的结束点y
    //@return 1:上  2:下  3:左  4:右
    public int getDirByGes(float start_x,float start_y,float end_x,float end_y){
        boolean isLeftOrRight=(Math.abs(start_x-end_x)>Math.abs(start_y-end_y))?true:false;//是否左右
        if (isLeftOrRight){//左右
            boolean isLeft = start_x-end_x>0?true:false;
            if (isLeft){
                return 3;
            }else {
                return 4;
            }
        }else {//上下
            boolean isUp = start_y-end_y>0?true:false;
            if (isUp){
                return 1;
            }else {
                return 2;
            }
        }
    }
    //随机打乱顺序
    public void randomMove(){
        //打乱的次数
        for (int i=0;i<10;i++){
            //根据手势开始交换，无动画
            int type=(int)(Math.random()*4)+1;
            changeByDir(type,false);
        }
    }
    public void changeDataByImageView(final ImageView mImageView){
        changeDataByImageView(mImageView,true);
    }
    //利用动画结束之后交换两个方块的数据
    //@param mImageView点击的方块
    //@param isAnim true：有动画 false：没动画
    public void changeDataByImageView(final ImageView mImageView,boolean isAnim){
        if (isAnimRun){//如果动画已经开始，则不做交换操作
            return;
        }
        if (!isAnim){//如果没有动画
            GameData mGamedata = (GameData) mImageView.getTag();
            iv_null_ImageView.setImageBitmap(mGamedata.bm);
            GameData mNullGameData = (GameData) iv_null_ImageView.getTag();
            mNullGameData.bm=mGamedata.bm;
            mNullGameData.p_x=mGamedata.p_x;
            (mNullGameData).p_y=mGamedata.p_y;
            setNullImageView(mImageView);//设置当前点击的是空方块
            if (isGameStart){
                isGameOver();//成功时弹出一个toast
            }
            return;
        }
        //创建一个动画，设置好方向，移动的距离
        TranslateAnimation translateAnimation = null;
        if (mImageView.getX()>iv_null_ImageView.getX()){//当前点击的方块在空方块下边
            //往上移动
            translateAnimation=new TranslateAnimation(0.1f,-mImageView.getWidth(),0.1f,0.1f);
        }else if (mImageView.getX()<iv_null_ImageView.getX()){//当前点击的方块在空方块上边
            //往下移动
            translateAnimation=new TranslateAnimation(0.1f,mImageView.getWidth(),0.1f,0.1f);
        }else if (mImageView.getX()>iv_null_ImageView.getY()){//当前点击的方块在空方块右边
            //往左移动
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,-mImageView.getWidth());
        }else if (mImageView.getX()<iv_null_ImageView.getY()){//当前点击的方块在空方块左边
            //往右移动
            translateAnimation=new TranslateAnimation(0.1f,0.1f,0.1f,mImageView.getWidth());
        }
        //设置动画的时长
        translateAnimation.setDuration(70);
        //设置动画结束之后是否停留
        translateAnimation.setFillAfter(true);
        //设置动画结束之后真正的数据交换
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimRun=true;//动画开始
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isAnimRun=false;//动画结束
                //结束之后清除动画
                mImageView.clearAnimation();
                GameData mGameData = (GameData) mImageView.getTag();
                iv_null_ImageView.setImageBitmap(mGameData.bm);
                GameData mNullGameData = (GameData) iv_null_ImageView.getTag();
                mNullGameData.bm=mGameData.bm;
                mNullGameData.p_x=mGameData.p_x;
                mNullGameData.p_y=mGameData.p_y;
                setNullImageView(mImageView);//设置当前点击的是空方块
                if (isGameStart){
                    isGameOver();//成功时弹一个toast
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        //执行动画
        mImageView.startAnimation(translateAnimation);
    }
    //设置某个方块为空方块
    //@param mImageView当前要设置为空的方块的实例
    public void setNullImageView(ImageView mImageView){
        mImageView.setImageBitmap(null);//设置为空
        iv_null_ImageView=mImageView;
    }
    //判断当前点击的方块，是否与空方块的位置关系是否是相邻关系
    //@param mImageView所点击的方块
    //@return true：相邻 false：不相邻
    public boolean isHasByNullImageView(ImageView mImageView){
        //分别获取当前空方块的位置与点击方块的位置，通过x，y两边都差1的方法判断
        GameData mNullGameData=(GameData) iv_null_ImageView.getTag();//空方块身上的数据
        GameData mGameData= (GameData) mImageView.getTag();//点击方块身上的数据
        if (mNullGameData.y==mGameData.y&&mGameData.x+1==mNullGameData.x){//当前点击的方块在空方块的上边
            return true;
        }else if (mNullGameData.y==mGameData.y&&mGameData.x-1==mNullGameData.x){//当前点击的方块在空方块下边
            return true;
        }else if (mNullGameData.y==mGameData.y+1&&mGameData.x==mGameData.x){//当前点击的方块在空方块左边
            return true;
        }else if (mNullGameData.y==mGameData.y-1&&mGameData.x+1==mGameData.x){//当前点击的方块在空方块右边
            return true;
        }
        return false;
    }
    //在每个游戏小方块上要绑定的数据
    class GameData{
        //每个小方块的实际位置x
        public int x=0;
        //每个小方块的实际位置y
        public int y=0;
        //每个小方块的图片
        public Bitmap bm;
        //每个小方块的图片位置x
        public int p_x=0;
        //每个小方块的图片位置y
        public int p_y=0;

        public GameData(int x,int y,Bitmap bm){
            this.x=x;
            this.y=y;
            this.bm=bm;
            this.p_x=x;
            this.p_y=y;
        }
        //每个小方块的位置是否正确
        //@return true：正确，false：不正确
        public boolean isTrue(){
            if (x==p_x&&y==p_y){
                return true;
            }
            return false;
        }
    }
}