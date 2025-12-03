/**
 * 小程序初始化参数
 */
export declare interface InitializeProps {
  // 胶囊按钮的标题和标识
  items?: { title: string; key: string }[];
  // 是否显示胶囊按钮
  capsule?: boolean;
  // 胶囊按钮字体大小
  fontSize?: string;
  // 胶囊按钮字体颜色
  fontColor?: string;
  // 胶囊按钮字体宽度
  fontWeight?: string;
  // 设置小程序退出时是否进入后台
  isEnableBackground?: boolean;
}

/**
 * 胶囊按钮样式
 */
export declare interface ICapsuleBtnStyleProps {
  // 背景颜色
  backgroundColor?: string;
  // 字体颜色
  textColor?: string;
  // 高亮颜色
  highlightColor?: string;
  // 边框颜色
  borderColor?: string;
}

export declare interface IExtraDataProps {
  // 是否支持暗色模式
  darkMode?: 'auto' | 'dark' | 'light';
}

/**
 * 开启小程序时传入的配置参数
 */
export declare interface IConfigurationProps {
  // 配置启动小程序时传递的参数
  extraData?: string | object | IExtraDataProps;
  // 小程序启动方式
  openMode?: 'DCUniMPOpenModePresent' | 'DCUniMPOpenModePush';
  // 是否开启侧滑手势关闭小程序
  enableGestureClose?: boolean;
  // 是否开启后台运行
  enableBackground?: boolean;
  // 可用于页面直达等操作的地址
  redirectPath?: string;
}

/**
 * uni小程序版本信息
 */
export declare interface IAppVersionInfoProps {
  // versionName
  name: string;
  // versionCode
  code: string;
}

/**
 * 小程序胶囊按钮点击回调参数
 */
export declare interface IMenuClickCallBackProps {
  // uni小程序应用id
  appid: string;
  // 小程序菜单按钮id
  buttonid: string;
}
