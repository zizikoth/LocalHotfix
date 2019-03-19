# 热修复（Tinker）
## 一、这个是什么东西  
### 正常开发流程    
版本1.0上线     
-> 发现Bug -> 修复Bug -> 发布版本1.1 -> 用户下载安装      
-> 发现Bug -> 修复Bug -> 发布版本1.2 -> 用户下载安装         
-> 发现Bug -> 修复Bug -> 发布版本1.3 -> 用户下载安装          
-> 应用更新 -> 发布版本1.4 
### 热修复开发流程  
版本1.0上线     
-> 发现Bug -> 修复Bug -> 发布补丁 -> 应用自动修复      
-> 发现Bug -> 修复Bug -> 发布补丁 -> 应用自动修复  
-> 发现Bug -> 修复Bug -> 发布补丁 -> 应用自动修复      
-> 应用更新 -> 发布版本1.1

## 二、原理
App中所有的类都是从dex中获取的，通过BaseDexClassLoader的findClass(String name)方法获取，dex可以通过AndroidStudio分析Apk看到，也可以解压apk看到。  
        
我们可以从下面两段代码中看到和一些命名中可以很容易的看出App找一个类的时候是从dex列表中一个一个的遍历如果找到了就返回这个类，没有找到会抛出ClassNotFoundExcepton。而Tinker热修复的原理就是通过添加一个补丁Dex来让找类的时候先通过查找这个补丁Dex中的类，如果找到类那么就直接返回找到的类而不会继续向下寻找后面的出现Bug的Dex，这就是插桩，再来一张我画的图吧就更容易理解了    

然后还有一点，我个人认为热修复和Windows的补丁（开启自动修复）类似，首先你需要开机，然后请求微软的某个接口，知道现在需要打补丁了，然后开始下载，下载完成后进行更新。热更新也是同样的，进入App之后请求后台，发现当前版本有一个补丁，之后下载补丁到本地，热更新工具发现本地补丁文件夹下面有一个补丁，那么就会在初始化的时候把这个补丁dex拿过来插入pathList中，来进行修复bug的功能实现

```
public class BaseDexClassLoader extends ClassLoader {
    //这个就是存放所有的dex
    private final DexPathList pathList;

    ...

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Throwable> suppressedExceptions = new ArrayList<Throwable>();
        Class c = pathList.findClass(name, suppressedExceptions);
        if (c == null) {
            ClassNotFoundException cnfe = new ClassNotFoundException("Didn't find class \"" + name + "\" on path: " + pathList);
            for (Throwable t : suppressedExceptions) {
                cnfe.addSuppressed(t);
            }
            throw cnfe;
        }
        return c;
    }
    
    ...
    
}
```

``` 
final class DexPathList {

    ...

    public Class findClass(String name, List<Throwable> suppressed) {
        for (Element element : dexElements) {
            DexFile dex = element.dexFile;

            if (dex != null) {
            Class clazz = dex.loadClassBinaryName(name, definingContext, suppressed);
                if (clazz != null) {
                    return clazz;
                    }
            }
        }
        if (dexElementsSuppressedExceptions != null) {
           suppressed.addAll(Arrays.asList(dexElementsSuppressedExceptions));
        }
       return null;
    }
    
    ...
    
}
    
```

![插桩.jpg](https://upload-images.jianshu.io/upload_images/4356451-5b0b47efedc6fa04.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## 三、代码演示
这里我们先创建一个App，将一些主要文件放入classes.dex，将bug文件放入classes2.dex，然后在其中创建一个bug，打包安装，此时装入手机的是Bug包，出现bug的文件类在classes2.dex中。  
然后我们修复bug，buildApk，将apk解压拿到classess2.dex，这个就类似补丁包，实际上的补丁包会更小，只是本地测试使用，将这个补丁包复制到App的私有目录中模拟从服务器中下载。        
点击修复按钮，将文件复制，然后退出应用（杀死进程），重新打开App就可以看到修复后的效果
##### 一、我们需要App有多个dex包，至少有一个主包，一个bug包，先进行分包处理
```
App

class App:Application(){
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        //加载补丁包
        HotfixUtils.loadFixedDex(this)
    }
}
```
```
build.gradle中对主包进行一些配置

multiDexEnabled true
multiDexKeepFile file('MultiDexKeep.txt')


    dexOptions{
        javaMaxHeapSize "4g"
        preDexLibraries = false
        additionalParameters = [
                '--multi-dex',
                '--set-max-idx-number=50000',
                '--main-dex-list='+'/MultiDexKeep.txt',
                '--minimal-main-dex'
        ]
    }
```
```
MultiDexKeep.txt 把一些不会出错的文件keep住 放到主包里

com/memo/hotfix/App.class
com/memo/hotfix/BaseActivity.class
com/memo/hotfix/utils/ArrayUtils.class
com/memo/hotfix/utils/Constant.class
com/memo/hotfix/utils/FileUtils.class
com/memo/hotfix/utils/HotfixUtils.class
com/memo/hotfix/utils/LogUtils.class
com/memo/hotfix/utils/ReflectUtils.class
```

##### 2、创建一个出现Bug的Activity，后续方便修改
```
 BugActivity

 override fun initialize() {
        ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        //模拟bug 点击不做任何操作
        mBtnShow.setOnClickListener {
            //这里模拟有bug
            mTvResult.text = "这里有Bug"
            //mTvResult.text = "Bug修复了"
        }

        //点击修复bug
        mBtnFix.setOnClickListener { fixBug() }
    }

    private fun fixBug() {
        //将补丁包patch.dex放到手机的根目录下面 然后将文件从本地复制到私有目录下面
        //实际上是从服务器下载到本地目录下面
        val from: File = File(Environment.getExternalStorageDirectory(), Constant.PATCH_DEX)
        val to: File =
            File(getDir(Constant.DEX_DIR, Context.MODE_PRIVATE).absolutePath + File.separator + Constant.PATCH_DEX)
        if (to.exists()) {
            //如果之前的补丁包存在 就删除
            val isDelete = to.delete()
            LogUtils.i("补丁包删除$isDelete")
        }

        if (from.exists()) {
            //复制 模拟服务器下载
            FileUtils.copy(from, to)
            showToast("补丁加载成功")
            LogUtils.i("copy成功${to.exists()}")
            from.delete()
        } else {
            showToast("补丁包不存在")
        }
    }
```
##### 3、划重点，我们在App中进行加载补丁包重点就是为了插桩，先获取补丁patchDexElement，在获取原有的oriDexElement，然后合并生成心得dexElement（顺序为补丁在前，原有在后），然后赋值给App的pathList里的dexElement，当然需要使用反射
```
object HotfixUtils {

    /*** 补丁包集合 ***/
    private val patchDexSet: HashSet<File> by lazy { HashSet<File>() }

    fun loadFixedDex(mContext: Context) {
        //先清空
        patchDexSet.clear()
        //获取补丁包目录
        val patchDexDir: File = mContext.getDir(Constant.DEX_DIR, Context.MODE_PRIVATE)
        //遍历这个补丁包下面的所有的文件
        val listFiles: Array<File> = patchDexDir.listFiles()
        for (file in listFiles) {
            if (file.name.endsWith(Constant.DEX_SUFFIX) && Constant.MAIN_DEX != file.name) {
                //找到文件夹下面的补丁包 放入自己的补丁包集合中
                patchDexSet.add(file)
            }
        }
        if (patchDexSet.size > 0) {
            //类加载器加载
            createDexClassLoader(mContext, patchDexDir)
        }
    }

    private fun createDexClassLoader(mContext: Context, patchDexDir: File) {
        //临时dex解压目录 因为类加载器加载的是类而不是dex 所以需要将dex进行解压
        val optDirPath: String = patchDexDir.absolutePath + File.separator + Constant.DEX_OPT
        //创建
        val optDir = File(optDirPath)
        if (!optDir.exists()) {
            optDir.mkdirs()
        }
        for (dex in patchDexSet) {
            //自己创建一个补丁DexClassLoader
            val patchClassLoader = DexClassLoader(dex.absolutePath, optDirPath, null, mContext.classLoader)
            //每次获取一个补丁文件，需要插桩一次

            //⚠️⚠️⚠️！！！最重要的环节！！！⚠️⚠️⚠️
            hotFix(patchClassLoader, mContext)
        }

    }

    /**
     * ⚠️⚠️⚠️！！！最重要的环节！！！⚠️⚠️⚠️
     * ⚠️⚠️⚠️！！！最重要的环节！！！⚠️⚠️⚠️
     * ⚠️⚠️⚠️！！！最重要的环节！！！⚠️⚠️⚠️
     */
    private fun hotFix(patchClassLoader: DexClassLoader, mContext: Context) {
        //这里分为6步
        //1.获取原有的PathClassLoader
        val pathClassLoader: PathClassLoader = mContext.classLoader as PathClassLoader

        try {

            //2.获取补丁包列表 dexElement
            val patchDexElement = ReflectUtils.getDexElement(ReflectUtils.getPathList(patchClassLoader))

            //3.获取原有的pathList
            val oriPathList = ReflectUtils.getPathList(pathClassLoader)

            //4.获取原有包列表 dexElement
            val oriDexElement = ReflectUtils.getDexElement(oriPathList)

            //5.合并成为一个新的 补丁包在前 原有包在后的dexElement
            val finalDexElement = ArrayUtils.combineArray(patchDexElement, oriDexElement)

            //6.用合成后的dexElement重新赋值原有的pathList里面的dexElement属性
            ReflectUtils.setDexElement(oriPathList, oriPathList.javaClass, finalDexElement)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```
##### 4、实际效果
![show.gif](https://upload-images.jianshu.io/upload_images/4356451-c46ff437a111b09f.gif?imageMogr2/auto-orient/strip)
