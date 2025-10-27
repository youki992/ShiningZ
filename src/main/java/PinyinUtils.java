import java.util.*;

/**
 * 中文转拼音工具类 - 离线版本
 * 支持常见中文字符的拼音转换
 */
public class PinyinUtils {
    
    // 常见中文字符拼音映射表
    private static final Map<Character, String> PINYIN_MAP = new HashMap<>();
    
    static {
        // 常见姓氏拼音
        PINYIN_MAP.put('张', "zhang");
        PINYIN_MAP.put('王', "wang");
        PINYIN_MAP.put('李', "li");
        PINYIN_MAP.put('赵', "zhao");
        PINYIN_MAP.put('刘', "liu");
        PINYIN_MAP.put('陈', "chen");
        PINYIN_MAP.put('杨', "yang");
        PINYIN_MAP.put('黄', "huang");
        PINYIN_MAP.put('周', "zhou");
        PINYIN_MAP.put('吴', "wu");
        PINYIN_MAP.put('徐', "xu");
        PINYIN_MAP.put('孙', "sun");
        PINYIN_MAP.put('马', "ma");
        PINYIN_MAP.put('朱', "zhu");
        PINYIN_MAP.put('胡', "hu");
        PINYIN_MAP.put('郭', "guo");
        PINYIN_MAP.put('何', "he");
        PINYIN_MAP.put('高', "gao");
        PINYIN_MAP.put('林', "lin");
        PINYIN_MAP.put('罗', "luo");
        PINYIN_MAP.put('郑', "zheng");
        PINYIN_MAP.put('梁', "liang");
        PINYIN_MAP.put('谢', "xie");
        PINYIN_MAP.put('宋', "song");
        PINYIN_MAP.put('唐', "tang");
        PINYIN_MAP.put('许', "xu");
        PINYIN_MAP.put('韩', "han");
        PINYIN_MAP.put('冯', "feng");
        PINYIN_MAP.put('邓', "deng");
        PINYIN_MAP.put('曹', "cao");
        PINYIN_MAP.put('彭', "peng");
        PINYIN_MAP.put('曾', "zeng");
        PINYIN_MAP.put('萧', "xiao");
        PINYIN_MAP.put('田', "tian");
        PINYIN_MAP.put('董', "dong");
        PINYIN_MAP.put('袁', "yuan");
        PINYIN_MAP.put('潘', "pan");
        PINYIN_MAP.put('于', "yu");
        PINYIN_MAP.put('蒋', "jiang");
        PINYIN_MAP.put('蔡', "cai");
        PINYIN_MAP.put('余', "yu");
        PINYIN_MAP.put('杜', "du");
        PINYIN_MAP.put('叶', "ye");
        PINYIN_MAP.put('程', "cheng");
        PINYIN_MAP.put('苏', "su");
        PINYIN_MAP.put('魏', "wei");
        PINYIN_MAP.put('吕', "lv");
        PINYIN_MAP.put('丁', "ding");
        PINYIN_MAP.put('任', "ren");
        PINYIN_MAP.put('沈', "shen");
        PINYIN_MAP.put('姚', "yao");
        PINYIN_MAP.put('卢', "lu");
        PINYIN_MAP.put('姜', "jiang");
        PINYIN_MAP.put('崔', "cui");
        PINYIN_MAP.put('钟', "zhong");
        PINYIN_MAP.put('谭', "tan");
        PINYIN_MAP.put('陆', "lu");
        PINYIN_MAP.put('汪', "wang");
        PINYIN_MAP.put('范', "fan");
        PINYIN_MAP.put('金', "jin");
        PINYIN_MAP.put('石', "shi");
        PINYIN_MAP.put('廖', "liao");
        PINYIN_MAP.put('贾', "jia");
        PINYIN_MAP.put('夏', "xia");
        PINYIN_MAP.put('韦', "wei");
        PINYIN_MAP.put('傅', "fu");
        PINYIN_MAP.put('方', "fang");
        PINYIN_MAP.put('白', "bai");
        PINYIN_MAP.put('邹', "zou");
        PINYIN_MAP.put('孟', "meng");
        PINYIN_MAP.put('熊', "xiong");
        PINYIN_MAP.put('秦', "qin");
        PINYIN_MAP.put('邱', "qiu");
        PINYIN_MAP.put('江', "jiang");
        PINYIN_MAP.put('尹', "yin");
        PINYIN_MAP.put('薛', "xue");
        PINYIN_MAP.put('闫', "yan");
        PINYIN_MAP.put('段', "duan");
        PINYIN_MAP.put('雷', "lei");
        PINYIN_MAP.put('侯', "hou");
        PINYIN_MAP.put('龙', "long");
        PINYIN_MAP.put('史', "shi");
        PINYIN_MAP.put('陶', "tao");
        PINYIN_MAP.put('黎', "li");
        PINYIN_MAP.put('贺', "he");
        PINYIN_MAP.put('顾', "gu");
        PINYIN_MAP.put('毛', "mao");
        PINYIN_MAP.put('郝', "hao");
        PINYIN_MAP.put('龚', "gong");
        PINYIN_MAP.put('邵', "shao");
        PINYIN_MAP.put('万', "wan");
        PINYIN_MAP.put('钱', "qian");
        PINYIN_MAP.put('严', "yan");
        PINYIN_MAP.put('覃', "qin");
        PINYIN_MAP.put('武', "wu");
        PINYIN_MAP.put('戴', "dai");
        PINYIN_MAP.put('莫', "mo");
        PINYIN_MAP.put('孔', "kong");
        PINYIN_MAP.put('向', "xiang");
        PINYIN_MAP.put('汤', "tang");
        
        // 常见名字用字
        PINYIN_MAP.put('伟', "wei");
        PINYIN_MAP.put('芳', "fang");
        PINYIN_MAP.put('娜', "na");
        PINYIN_MAP.put('敏', "min");
        PINYIN_MAP.put('静', "jing");
        PINYIN_MAP.put('丽', "li");
        PINYIN_MAP.put('强', "qiang");
        PINYIN_MAP.put('磊', "lei");
        PINYIN_MAP.put('军', "jun");
        PINYIN_MAP.put('洋', "yang");
        PINYIN_MAP.put('勇', "yong");
        PINYIN_MAP.put('艳', "yan");
        PINYIN_MAP.put('杰', "jie");
        PINYIN_MAP.put('娟', "juan");
        PINYIN_MAP.put('涛', "tao");
        PINYIN_MAP.put('明', "ming");
        PINYIN_MAP.put('超', "chao");
        PINYIN_MAP.put('秀', "xiu");
        PINYIN_MAP.put('英', "ying");
        PINYIN_MAP.put('华', "hua");
        PINYIN_MAP.put('慧', "hui");
        PINYIN_MAP.put('巧', "qiao");
        PINYIN_MAP.put('美', "mei");
        PINYIN_MAP.put('娜', "na");
        PINYIN_MAP.put('平', "ping");
        PINYIN_MAP.put('雅', "ya");
        PINYIN_MAP.put('丹', "dan");
        PINYIN_MAP.put('武', "wu");
        PINYIN_MAP.put('文', "wen");
        PINYIN_MAP.put('红', "hong");
        PINYIN_MAP.put('玉', "yu");
        PINYIN_MAP.put('梅', "mei");
        PINYIN_MAP.put('虹', "hong");
        PINYIN_MAP.put('学', "xue");
        PINYIN_MAP.put('志', "zhi");
        PINYIN_MAP.put('清', "qing");
        PINYIN_MAP.put('飞', "fei");
        PINYIN_MAP.put('雪', "xue");
        PINYIN_MAP.put('良', "liang");
        PINYIN_MAP.put('影', "ying");
        PINYIN_MAP.put('毅', "yi");
        PINYIN_MAP.put('珍', "zhen");
        PINYIN_MAP.put('健', "jian");
        PINYIN_MAP.put('辉', "hui");
        PINYIN_MAP.put('刚', "gang");
        PINYIN_MAP.put('亮', "liang");
        PINYIN_MAP.put('正', "zheng");
        PINYIN_MAP.put('瑞', "rui");
        PINYIN_MAP.put('锋', "feng");
        PINYIN_MAP.put('波', "bo");
        PINYIN_MAP.put('斌', "bin");
        PINYIN_MAP.put('富', "fu");
        PINYIN_MAP.put('顺', "shun");
        PINYIN_MAP.put('信', "xin");
        PINYIN_MAP.put('子', "zi");
        PINYIN_MAP.put('杰', "jie");
        PINYIN_MAP.put('涛', "tao");
        PINYIN_MAP.put('昌', "chang");
        PINYIN_MAP.put('成', "cheng");
        PINYIN_MAP.put('康', "kang");
        PINYIN_MAP.put('星', "xing");
        PINYIN_MAP.put('光', "guang");
        PINYIN_MAP.put('天', "tian");
        PINYIN_MAP.put('达', "da");
        PINYIN_MAP.put('安', "an");
        PINYIN_MAP.put('岩', "yan");
        PINYIN_MAP.put('中', "zhong");
        PINYIN_MAP.put('茂', "mao");
        PINYIN_MAP.put('进', "jin");
        PINYIN_MAP.put('林', "lin");
        PINYIN_MAP.put('有', "you");
        PINYIN_MAP.put('坚', "jian");
        PINYIN_MAP.put('和', "he");
        PINYIN_MAP.put('彪', "biao");
        PINYIN_MAP.put('博', "bo");
        PINYIN_MAP.put('诚', "cheng");
        PINYIN_MAP.put('先', "xian");
        PINYIN_MAP.put('敬', "jing");
        PINYIN_MAP.put('震', "zhen");
        PINYIN_MAP.put('振', "zhen");
        PINYIN_MAP.put('壮', "zhuang");
        PINYIN_MAP.put('会', "hui");
        PINYIN_MAP.put('思', "si");
        PINYIN_MAP.put('群', "qun");
        PINYIN_MAP.put('豪', "hao");
        PINYIN_MAP.put('心', "xin");
        PINYIN_MAP.put('邦', "bang");
        PINYIN_MAP.put('承', "cheng");
        PINYIN_MAP.put('乐', "le");
        PINYIN_MAP.put('绍', "shao");
        PINYIN_MAP.put('功', "gong");
        PINYIN_MAP.put('松', "song");
        PINYIN_MAP.put('善', "shan");
        PINYIN_MAP.put('厚', "hou");
        PINYIN_MAP.put('庆', "qing");
        PINYIN_MAP.put('磊', "lei");
        PINYIN_MAP.put('民', "min");
        PINYIN_MAP.put('友', "you");
        PINYIN_MAP.put('裕', "yu");
        PINYIN_MAP.put('河', "he");
        PINYIN_MAP.put('哲', "zhe");
        PINYIN_MAP.put('江', "jiang");
        PINYIN_MAP.put('超', "chao");
        PINYIN_MAP.put('浩', "hao");
        PINYIN_MAP.put('亮', "liang");
        PINYIN_MAP.put('政', "zheng");
        PINYIN_MAP.put('谦', "qian");
        PINYIN_MAP.put('亨', "heng");
        PINYIN_MAP.put('奇', "qi");
        PINYIN_MAP.put('固', "gu");
        PINYIN_MAP.put('之', "zhi");
        PINYIN_MAP.put('轮', "lun");
        PINYIN_MAP.put('翰', "han");
        PINYIN_MAP.put('朗', "lang");
        PINYIN_MAP.put('伯', "bo");
        PINYIN_MAP.put('宏', "hong");
        PINYIN_MAP.put('言', "yan");
        PINYIN_MAP.put('若', "ruo");
        PINYIN_MAP.put('鸣', "ming");
        PINYIN_MAP.put('朋', "peng");
        PINYIN_MAP.put('斌', "bin");
        PINYIN_MAP.put('梁', "liang");
        PINYIN_MAP.put('栋', "dong");
        PINYIN_MAP.put('维', "wei");
        PINYIN_MAP.put('启', "qi");
        PINYIN_MAP.put('克', "ke");
        PINYIN_MAP.put('伦', "lun");
        PINYIN_MAP.put('翔', "xiang");
        PINYIN_MAP.put('旭', "xu");
        PINYIN_MAP.put('鹏', "peng");
        PINYIN_MAP.put('泽', "ze");
        PINYIN_MAP.put('晨', "chen");
        PINYIN_MAP.put('辰', "chen");
        PINYIN_MAP.put('士', "shi");
        PINYIN_MAP.put('以', "yi");
        PINYIN_MAP.put('建', "jian");
        PINYIN_MAP.put('家', "jia");
        PINYIN_MAP.put('致', "zhi");
        PINYIN_MAP.put('树', "shu");
        PINYIN_MAP.put('炎', "yan");
        PINYIN_MAP.put('德', "de");
        PINYIN_MAP.put('行', "xing");
        PINYIN_MAP.put('时', "shi");
        PINYIN_MAP.put('泰', "tai");
        PINYIN_MAP.put('盛', "sheng");
        PINYIN_MAP.put('雄', "xiong");
        PINYIN_MAP.put('琛', "chen");
        PINYIN_MAP.put('钧', "jun");
        PINYIN_MAP.put('冠', "guan");
        PINYIN_MAP.put('策', "ce");
        PINYIN_MAP.put('腾', "teng");
        PINYIN_MAP.put('楠', "nan");
        PINYIN_MAP.put('榕', "rong");
        PINYIN_MAP.put('风', "feng");
        PINYIN_MAP.put('航', "hang");
        PINYIN_MAP.put('弘', "hong");
        
        // 数字
        PINYIN_MAP.put('一', "yi");
        PINYIN_MAP.put('二', "er");
        PINYIN_MAP.put('三', "san");
        PINYIN_MAP.put('四', "si");
        PINYIN_MAP.put('五', "wu");
        PINYIN_MAP.put('六', "liu");
        PINYIN_MAP.put('七', "qi");
        PINYIN_MAP.put('八', "ba");
        PINYIN_MAP.put('九', "jiu");
        PINYIN_MAP.put('十', "shi");
        PINYIN_MAP.put('零', "ling");
        PINYIN_MAP.put('百', "bai");
        PINYIN_MAP.put('千', "qian");
        PINYIN_MAP.put('万', "wan");
    }
    
    /**
     * 获取单个字符的拼音
     */
    public static String getCharPinyin(char ch) {
        return PINYIN_MAP.getOrDefault(ch, String.valueOf(ch));
    }
    
    /**
     * 转换为全拼小写无空格
     */
    public static String toFullPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char ch : chinese.toCharArray()) {
            String pinyin = getCharPinyin(ch);
            result.append(pinyin);
        }
        return result.toString().toLowerCase();
    }
    
    /**
     * 转换为点分隔全拼
     */
    public static String toDotSeparatedPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        List<String> pinyinList = new ArrayList<>();
        for (char ch : chinese.toCharArray()) {
            String pinyin = getCharPinyin(ch);
            if (!pinyin.equals(String.valueOf(ch))) { // 只有中文字符才添加
                pinyinList.add(pinyin.toLowerCase());
            }
        }
        return String.join(".", pinyinList);
    }
    
    /**
     * 转换为首字母缩写
     */
    public static String toFirstLetters(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char ch : chinese.toCharArray()) {
            String pinyin = getCharPinyin(ch);
            if (!pinyin.isEmpty()) {
                result.append(pinyin.charAt(0));
            }
        }
        return result.toString().toLowerCase();
    }
    
    /**
     * 转换为驼峰式
     */
    public static String toCamelCase(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (char ch : chinese.toCharArray()) {
            String pinyin = getCharPinyin(ch);
            if (!pinyin.isEmpty()) {
                result.append(pinyin.substring(0, 1).toUpperCase())
                      .append(pinyin.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }
    
    /**
     * 转换为姓在后全拼（适用于姓名）
     */
    public static String toReversedPinyin(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        if (chinese.length() < 2) {
            return toFullPinyin(chinese);
        }
        
        // 假设第一个字是姓，其余是名
        String surname = chinese.substring(0, 1);
        String givenName = chinese.substring(1);
        
        return toFullPinyin(givenName) + toFullPinyin(surname);
    }
    
    /**
     * 转换为首字母加点+名
     */
    public static String toFirstLetterDotName(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        if (chinese.length() < 2) {
            return toFullPinyin(chinese);
        }
        
        // 假设第一个字是姓，其余是名
        String surname = chinese.substring(0, 1);
        String givenName = chinese.substring(1);
        
        String surnameFirstLetter = toFirstLetters(surname);
        String givenNamePinyin = toFullPinyin(givenName);
        
        return surnameFirstLetter + "." + givenNamePinyin;
    }
    
    /**
     * 转换为首字母大写拼接
     */
    public static String toFirstLetterUppercase(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        if (chinese.length() < 2) {
            return toFullPinyin(chinese);
        }
        
        // 假设第一个字是姓，其余是名
        String surname = chinese.substring(0, 1);
        String givenName = chinese.substring(1);
        
        String surnameFirstLetter = toFirstLetters(surname).toUpperCase();
        String givenNamePinyin = toFullPinyin(givenName);
        
        return surnameFirstLetter + givenNamePinyin;
    }
    
    /**
     * 转换为姓大写+名小写
     */
    public static String toSurnameUpperGivenLower(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }
        
        if (chinese.length() < 2) {
            return toFullPinyin(chinese).toUpperCase();
        }
        
        // 假设第一个字是姓，其余是名
        String surname = chinese.substring(0, 1);
        String givenName = chinese.substring(1);
        
        String surnamePinyin = toFullPinyin(surname).toUpperCase();
        String givenNamePinyin = toFullPinyin(givenName).toLowerCase();
        
        return surnamePinyin + givenNamePinyin;
    }
    
    /**
     * 生成所有格式的拼音变体
     */
    public static List<String> generateAllVariants(String chinese) {
        List<String> variants = new ArrayList<>();
        
        if (chinese == null || chinese.isEmpty()) {
            return variants;
        }
        
        // 添加原始中文
        variants.add(chinese);
        
        // 添加各种拼音格式
        String fullPinyin = toFullPinyin(chinese);
        if (!fullPinyin.isEmpty() && !variants.contains(fullPinyin)) {
            variants.add(fullPinyin);
        }
        
        String dotSeparated = toDotSeparatedPinyin(chinese);
        if (!dotSeparated.isEmpty() && !variants.contains(dotSeparated)) {
            variants.add(dotSeparated);
        }
        
        String firstLetters = toFirstLetters(chinese);
        if (!firstLetters.isEmpty() && !variants.contains(firstLetters)) {
            variants.add(firstLetters);
        }
        
        String camelCase = toCamelCase(chinese);
        if (!camelCase.isEmpty() && !variants.contains(camelCase)) {
            variants.add(camelCase);
        }
        
        // 对于姓名（长度大于1），添加更多变体
        if (chinese.length() > 1) {
            String reversed = toReversedPinyin(chinese);
            if (!reversed.isEmpty() && !variants.contains(reversed)) {
                variants.add(reversed);
            }
            
            String firstLetterDotName = toFirstLetterDotName(chinese);
            if (!firstLetterDotName.isEmpty() && !variants.contains(firstLetterDotName)) {
                variants.add(firstLetterDotName);
            }
            
            String firstLetterUpper = toFirstLetterUppercase(chinese);
            if (!firstLetterUpper.isEmpty() && !variants.contains(firstLetterUpper)) {
                variants.add(firstLetterUpper);
            }
            
            String surnameUpperGivenLower = toSurnameUpperGivenLower(chinese);
            if (!surnameUpperGivenLower.isEmpty() && !variants.contains(surnameUpperGivenLower)) {
                variants.add(surnameUpperGivenLower);
            }
        }
        
        return variants;
    }
    
    /**
     * 检查字符是否为中文
     */
    public static boolean isChinese(char ch) {
        return PINYIN_MAP.containsKey(ch);
    }
    
    /**
     * 检查字符串是否包含中文
     */
    public static boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (char ch : text.toCharArray()) {
            if (isChinese(ch)) {
                return true;
            }
        }
        return false;
    }
}