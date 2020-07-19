### 说明

本文将介绍element-ui的一般用法，以及文档的正确阅读姿势，还有就是一些使用过程中遇到一些坑的总结（~~说白了就是用的不够熟练...不过坑的确还是有的...~~）
 官方链接：[https://element.eleme.cn/2.0/#/zh-CN/component/installation](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Finstallation)

### 一般组件使用

element-ui的基本使用还是十分简单的，首先根据文档的步骤进行安装，导入需要的样式即可，这一块直接看文档就已经说明的很清楚了：[文档链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Fquickstart)。接着，你只需要找到你想要的样式，然后点开详细代码，复制到你的`.vue`文件里那么就可以了，例如直接复制官方提供的radio组件([链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Fradio))的第一个代码：



```xml
<template>
  <div>
      <!-- 因为template里面只能有一个根元素，官方这里拷过来以后在外面加了一层div标签包起来 -->
    <el-radio v-model="radio" label="1">备选项</el-radio>
    <el-radio v-model="radio" label="2">备选项</el-radio>
  </div>
</template>

<script>
export default {
  data() {
    return {
      radio: "1"
    };
  }
};
</script>
```

可以看出element-ui的上手使用还是挺容易的，在理想情况下，只需要以下步骤：

1. 找想要的样式组件
2. 复制代码到对应的`.vue`文件
3. 修改对应的数据

### 非组件样式使用

官方提供的样式中有一部分并非是组件样式，比如：字体图标、全局指令、全局事件等，这里简单说明下这类样式的使用方法

##### 图标样式使用

官方提供了很多图标样式（[链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Ficon)），通过阅读文档可以发现：一般情况下，使用`i`标签，并将`class`属性设置成对应的图标名即可；而对于别的组件希望引入图标时（有`icon`属性的组件，例如`el-button`），设置`icon`属性为对应的图标名即可

##### 指令样式使用

例如`Loading加载`（[链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Floading)），其提供的是一个指令`v-loading`，而非一个组件，那么使用也很简单，因为已经注册了全局组件，直接调用就行了，举例：



```xml
<template>
  <div>
    <div v-loading="isLoading">这块内容使用v-loading指令，true时loading</div>
    <el-button @click=handle>{{clickText}}</el-button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isLoading: true,
      clickText: "取消loading"
    }
  },
  methods:{
      handle(){
          this.isLoading = !this.isLoading
          if (this.isLoading) {
              this.clickText = "取消loading"
          } else {
              this.clickText = "继续loading"
          }
      }
  }
};
</script>
```

##### 事件样式使用

例如`Message消息提示`（[链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Fmessage)），其提供是也不是组件，而是发送一个事件，那么使用时只需要参考文档发送的数据属性选项即可

### 文档读法

element-ui文档提供了很多示例代码，一般情况下我们直接拷下示例代码稍微看着改改数据之类的就够用了。但是在某些场景下，我们可能又需要使用到一些特殊的功能和属性，而这些功能属性一般在官方提供的组件中都已经内置了，所以我们可以直接先从文档中寻找查看是否有属性或者方法等能够满足我们的需求，从而避免重复造轮子。下面就来说明下文档里提供的属性、方法等如何阅读以及使用。

##### attribute读法

顾名思义，就是官方提供的属性，使用很简单，直接设置属性和对应的值就行了，直接拿`el-input`组件([链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Finput))来说，例如其提供的Input Attributes里面有`maxlength`（最大输入长度，`number`型）和`clearable`（是否可清空，`boolean`型），那么：**对于非`boolean`型的属性，需要直接设置值的内容；对于`boolean`型的属性，一般默认是`false`，而直接添加该属性，默认就是设置为`true`**，下面是使用了这两个属性的示例代码：



```xml
<template>
  <div>
    <el-input v-model="name" maxlength="10" clearable></el-input>
    <!-- 最大长度为10，设置可清空，添加该boolean属性即默认为true，等同于：clearable="true" -->
  </div>
</template>

<script>
export default {
  data() {
    return {
      name: "aaa"
    };
  }
};
</script>
```

##### slot读法

`slot`的使用和普通的`slot`用法没什么区别，直接在该组件当中定义一个标签，并设置该标签的`slot`属性即可，还是拿`el-input`组件举例，其Input slots下提供了`prepend`（输入框头部内容，只对 type="text" 有效），下面是使用了该`slot`的代码示例：



```xml
<template>
  <div>
    <el-input v-model="name">
      <template slot="prepend">aaa</template>
      <!-- 定义一个template标签，设置slot为对应的值，里面插入对应的内容，不一定要用template标签，别的也可以 -->
    </el-input>
  </div>
</template>

<script>
export default {
  data() {
    return {
      name: "aaa"
    };
  }
};
</script>
```

##### event读法

event就是提供的监听事件，直接使用`v-on`（简写`@`）绑定该event即可，例如`el-input`组件的Input Events下提供了`change`方法，并且看到回调参数里包括`value`，那么该事件将会传递这个参数，下面使用该事件举例：



```xml
<template>
  <div>
    <el-input v-model="name" @change="handleChange"></el-input>
    <!-- 监听change事件，并绑定对应的处理方法 -->
  </div>
</template>

<script>
export default {
  data() {
    return {
      name: "aaa"
    };
  },
  methods: {
    handleChange(value) {
      // 监听事件的处理方法，回调时会获取到修改后的值
      alert(`值被修改为：${value}`);
    }
  }
};
</script>
```

##### method读法

method就是该组件内置的方法，使用时可以通过`ref`属性调用，使用步骤如下：

- 组件设置`ref`属性（具体`ref`属性用法参考：[https://www.cnblogs.com/wu50401/p/10213081.html](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Fwu50401%2Fp%2F10213081.html)）
- 通过`this.$refs.ref属性名.方法()`调用组件自身的method方法

例如`el-input`组件提供了`focus()`方法，举例如下：



```xml
<template>
  <div>
    <el-input v-model="name" ref="nameInput"></el-input>
    <!-- 设置一个ref属性 -->
    <el-button @click="handle">focus in input</el-button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      name: "aaa"
    };
  },
  methods: {
    handle() {
      this.$refs.nameInput.focus();
      // 调用文档提供的focus方法聚焦到对应的组件
    }
  }
};
</script>
```

**注：**
 这里实际上就是父组件调用子组件方法的场景

##### option读法

option一般是一些attribute的类型为`object`类型时，该attribute对象里的键值参考，这里拿`el-time-select`组件（时间选择器，[链接](https://links.jianshu.com/go?to=https%3A%2F%2Felement.eleme.cn%2F2.0%2F%23%2Fzh-CN%2Fcomponent%2Ftime-picker)）举例，该组件里的`picker-options`属性是`object`类型，而文档也提供了Time Select Options的参数说明，下面是使用示例：



```xml
<template>
  <div>
    <el-time-select
      v-model="value1"
      :picker-options="{
        start: '08:30',
        step: '00:15',
        end: '18:30'
  }"
    ></el-time-select>
    <!-- 直接照搬官方文档提供了...这里加下说明：picker-options是object类型，option里则提供了各种该对象里的参数 -->
  </div>
</template>

<script>
export default {
  data() {
    return {
      value1: ""
    };
  }
};
</script>
```

##### 子组件读法

很多提供的组件里都含有子组件，比如`el-select`下就有子组件`el-option`，那么使用时只需要将其嵌套在父组件，而对应子组件的`attributes`、`slot`等的使用方式也是一样的，这里提供`el-select`结合子组件`el-option`的使用示例：



```xml
<template>
  <div>
    <el-select v-model="value" placeholder="请选择">
      <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
      <!-- 子组件el-option嵌套在父组件el-select中，其他都像普通组件一样使用 -->
    </el-select>
  </div>
</template>

<script>
export default {
  data() {
    return {
      options: [
        {
          value: "选项1",
          label: "黄金糕"
        },
        {
          value: "选项2",
          label: "双皮奶"
        }
      ],
      value: ""
    };
  }
};
</script>
```

### 常见问题

##### 样式修改不生效问题

element-ui本身提供了很多样式，但有时候想要稍微修改一下组件里面的样式，发现修改样式竟然不生效？
 别急，下面两种办法可以搞定：

- 方法一：在设置样式属性时，前面加上`>>>`来修改样式，但是这个在使用scss的时候可能不会生效，那么此时就使用方法二
- 方法二：使用`/deep/`（记得用空格隔开）来修改样式，本质上和`>>>`是一样的，相当于别名，举例：



```undefined
/deep/ .el-xxx {
  ...
}
```

- 方法三：如果上面的也都不行，就使用`::v-deep`（记得用空格隔开）试试，举例：



```ruby
::v-deep .el-xxx {
  ...
}
```

查了下原因，原来是因为由于提供的组件样式设置了`scope`关键字，导致其样式处于局部，我们无法随便更改，如果非要修改局部的样式，那么就使用上面提供的关键字来实现。
 （这也不算是element-ui的问题，在vue的组件化开发中就是规定如此，只不过平常自己开发的组件样式都可以自己规定，所以一般不会发现这个问题...）

element-ui导航栏报错：vue中的NavigationDuplicated {_name: "NavigationDuplicated", name: "NavigationDuplicated"}

当多次访问同一路由时会报的错，虽然报错了，但本身不影响使用，只需要将该报错屏蔽即可，参考：
 https://www.jianshu.com/p/af280206d57c

##### element-ui中组件方法重写问题

有些官方提供的组件其内部实现的函数并非是我们希望，比如`el-image`组件的click事件定义如下：



```dart
clickHandler() {
  // prevent body scroll
  prevOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  this.showViewer = true;
},
```

该事件导致的结果就是点击图片以后会发现页面无法滚动，让人产生一种页面卡死的错觉，因此我们可以通过以下方式来解决该问题：

1. 在使用组件的地方重写该方法，具体步骤就是通过`ref`绑定该组件，然后重写该组件的方法（其中this指向问题需要注意，我们可以通过`call`/`apply`/`bind`方式改变其指向），举例：



```kotlin
<el-image ... ref="test">
  ...
</el-image>
...
mounted() {
  // 重写clickHandler方法，并使用call方式在调用时修改指向
  this.$refs.test.clickHandler = () => {
    function test() {
      console.log(this.src);
    }
    test.call(this.$refs.test);
  };

  // 使用bind方式修改方法指向
  // this.$refs.test.clickHandler = function() {
  //   console.log(this.src);
  // }.bind(this.$refs.test);
},
```

1. 在引入Element前重写该方法，这种方式适合大量引用该组件时使用，避免代码的重复，举例：



```jsx
Element.Image.methods.clickHandler = function() {
  console.log(this.src);
};
...
Vue.use(Element);
```

1. 直接重写该组件（组件的定义在element-ui的package目录下），直接把对应的`.vue`文件拷下来改写，之后我们直接引入改写后的组件即可，这种方式适合对组件进行大改动的时候，一次改写，一劳永逸