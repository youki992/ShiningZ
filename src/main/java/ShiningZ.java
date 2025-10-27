import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.stream.Stream;

public class ShiningZ extends JFrame {

    /* ------------ 常量 ------------ */
    private static final String TEAM_URL = "https://www.code4th.cn";
    private static final String AI_URL   = "https://api.moonshot.cn/v1/chat/completions";

    /* ------------ 数据模型 ------------ */
    private final DefaultListModel<String> modelA = new DefaultListModel<>();
    private final DefaultListModel<String> modelB = new DefaultListModel<>();
    private final DefaultListModel<String> modelC = new DefaultListModel<>();

    private final JTextArea areaA = new JTextArea();
    private final JTextArea areaB = new JTextArea();
    private final JTextArea areaC = new JTextArea();

    private final JTextField patternField = new JTextField("AB,AC,BC,ABC");
    private final JTextField apiKeyField  = new JTextField(20);
    private final JCheckBox  aiCheck      = new JCheckBox("AI 增强");
    private final List<String> generated  = new ArrayList<>();
    private final boolean[] aiEnable = {false, false, false}; // A B C

    /* ------------ 新：AI 页字段 ------------ */
    private final JTextField  siteField   = new JTextField();
    private final JTextField  domainField = new JTextField();
    private final JTextField  nameField   = new JTextField();
    private final JTextField  phoneField  = new JTextField();
    private final JTextField  emailField  = new JTextField();
    private final JTextField  addrField   = new JTextField();
    private final JTextField  sidField    = new JTextField();   // 学号
    private final JTextField  pwdField    = new JTextField();   // 密码
    private final JTextField  qqField    = new JTextField();
    private final JTextField  wxField    = new JTextField();
    private final JTextField  sfzField    = new JTextField();
    private final JTextField  cpField    = new JTextField();
    private final JTextArea   customPromptArea = new JTextArea(4, 0);

    /* ------------ 界面引用 ------------ */
    private JComboBox<String> comboB;
    private JComboBox<String> comboC;
    private JTextArea appendArea;

    private final Map<String, JCheckBox> nameRuleCheckBoxes = new LinkedHashMap<>();
    private static final String[] NAME_RULES = {
            "全拼小写无空格",        // zhangsanfeng
            "姓在后全拼",           // sanfengzhang
            "点分隔全拼",           // zhang.sanfeng
            "首字母缩写",           // zsf
            "首字母加点+名",        // zs.feng
            "首字母大写拼接",       // ZSFeng
            "驼峰式",               // ZhangSanFeng
            "姓大写+名小写"         // ZHANGsanfeng
    };

    public ShiningZ() {
        FlatMacLightLaf.setup();
        setTitle("闪紫 - 社工字典生成器（社区版）v1.1      By C4安全 - chobits02");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createMenuBar();
        initTabs();          // 关键：3 个页签
        loadResourceDicts();
        apiKeyField.setText(loadKey());
    }

    /* ------------ 菜单栏 ------------ */
    private void createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("关于");
        JMenuItem item = new JMenuItem("团队官网");
        item.addActionListener(e -> {
            try { Desktop.getDesktop().browse(new URI(TEAM_URL)); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "无法打开浏览器"); }
        });
        menu.add(item);
        bar.add(menu);
        JMenu settingsMenu = new JMenu("设置");
        JMenuItem openSettings = new JMenuItem("AI 增强开关");
        openSettings.addActionListener(e -> showSettingsDialog());
        settingsMenu.add(openSettings);
        bar.add(settingsMenu);
        setJMenuBar(bar);
    }

    /* ------------ 页签化 ------------ */
    private void initTabs() {
        JTabbedPane tabs = new JTabbedPane();

        /* -------- 1. 字典生成页 -------- */
        JPanel genPanel = buildGenPanel();
        tabs.addTab("字典生成", genPanel);

        /* -------- 2. AI 增强页 -------- */
        JPanel aiPanel = buildAiPanel();
        tabs.addTab("AI 社工字典", aiPanel);

        /* -------- 3. 中文名转拼音页 -------- */
        JPanel pinyinPanel = buildPinyinPanel();
        tabs.addTab("中文转拼音", pinyinPanel);

        /* -------- 4. 常用密码生成页 -------- */
        JPanel passwordPanel = buildPasswordPanel();
        tabs.addTab("常用密码生成", passwordPanel);

        /* -------- 5. 数字序列生成页 -------- */
        JPanel numberPanel = buildNumberPanel();
        tabs.addTab("数字序列", numberPanel);

        /* -------- 6. 键盘走位页 -------- */
        JPanel keyboardPanel = buildKeyboardPanel();
        tabs.addTab("键盘走位", keyboardPanel);

        JPanel nameRulePanel = buildNameRulePanel();
        tabs.addTab("英文名转换配置", nameRulePanel);

        JPanel aboutPanel = buildAboutPanel();
        tabs.addTab("关于", aboutPanel);

        add(tabs);
    }

    /* ===================================================================================
       字典生成页（原功能，只是把原来的 buildUI() 搬进来，无其他改动）
       =================================================================================== */
    private JPanel buildGenPanel() {
        ImageIcon raw = new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/logo/logo.png")));
        Image scaled = raw.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaled);
        setIconImage(logoIcon.getImage());                // 任务栏小图标
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.add(logoLabel, BorderLayout.NORTH);

        /* 字典展示区（纵向） */
        JPanel dictPanel = new JPanel(new GridLayout(1, 3, 8, 8));
        dictPanel.add(createScrollPanel("A栏（自定义）", areaA));
        dictPanel.add(createScrollPanel("B栏（字典）",  areaB));
        dictPanel.add(createScrollPanel("C栏（日期）",  areaC));

        /* 右侧控制区 */
        JPanel eastPanel = new JPanel(new BorderLayout(5, 5));
        eastPanel.setPreferredSize(new Dimension(260, 0));

        JPanel comboPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        comboB = new JComboBox<>();
        comboC = new JComboBox<>();
        comboPanel.add(comboB);
        comboPanel.add(comboC);
        eastPanel.add(new JLabel("快速选择字典"), BorderLayout.NORTH);
        eastPanel.add(comboPanel, BorderLayout.CENTER);

        /* 底部参数区 */
        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.setBorder(new EmptyBorder(8, 0, 0, 0));

        appendArea = new JTextArea(3, 0);
        south.add(createScrollPanel("追加字典（可选）", appendArea), BorderLayout.SOUTH);

        JButton saveCfgBtn = new JButton("保存配置");
        saveCfgBtn.addActionListener(e -> saveKey(apiKeyField.getText().trim()));

        JPanel aiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aiPanel.add(new JLabel("Kimi Key:"));
        aiPanel.add(apiKeyField);
        aiPanel.add(aiCheck);
        aiPanel.add(saveCfgBtn);   // ← 就加这一句

        JPanel patternPanel = new JPanel(new BorderLayout(5, 5));
        patternPanel.add(new JLabel("组合模式（如 AB,AC,BC,CBA）："), BorderLayout.WEST);
        patternPanel.add(patternField, BorderLayout.CENTER);

        south.add(aiPanel, BorderLayout.NORTH);
        south.add(patternPanel, BorderLayout.CENTER);

        /* 按钮区 */
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton gen   = new JButton("生成字典");
        JButton dedup = new JButton("去重");
        JButton save  = new JButton("保存字典");
        gen.addActionListener(e -> generate());
        dedup.addActionListener(e -> dedup());
        save.addActionListener(e -> saveDict());
        btn.add(gen); btn.add(dedup); btn.add(save);
        JButton loadABtn = new JButton("加载A字典");
        JButton loadAppendBtn = new JButton("加载追加字典");
        btn.add(loadABtn);
        btn.add(loadAppendBtn);
        JButton loadLocalB = new JButton("本地加载 B 栏");
        loadLocalB.addActionListener(e -> loadFileToArea("选择本地 B 字典", areaB));
        btn.add(loadLocalB);
        JButton loadLocalC = new JButton("本地加载 C 栏");
        loadLocalC.addActionListener(e -> loadFileToArea("选择本地 C 字典", areaC));
        btn.add(loadLocalC);

        /* 组装 */
        root.add(dictPanel, BorderLayout.CENTER);
        root.add(eastPanel, BorderLayout.EAST);
        root.add(btn, BorderLayout.SOUTH);
        root.add(south, BorderLayout.NORTH);

        /* 下拉框事件 */
        comboB.addActionListener(e -> {
            String f = (String) comboB.getSelectedItem();
            if (f != null) loadDictToTextArea(f, areaB);
        });
        comboC.addActionListener(e -> {
            String f = (String) comboC.getSelectedItem();
            if (f != null) loadDictToTextArea(f, areaC);
        });
        loadABtn.addActionListener(e -> loadFileToArea("选择 A 字典", areaA));
        loadAppendBtn.addActionListener(e -> loadFileToArea("选择追加字典", appendArea));

        return root;
    }

    private JPanel buildNameRulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel checkPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        for (String rule : NAME_RULES) {
            JCheckBox cb = new JCheckBox(rule, false);
            nameRuleCheckBoxes.put(rule, cb);
            checkPanel.add(cb);
        }

        JLabel hint = new JLabel("<html>勾选后，AI 将利用这些规则生成中文名的英文变体。<br>仅当 AI 增强启用时生效。</html>");
        hint.setBorder(new EmptyBorder(10, 0, 10, 0));

        panel.add(hint, BorderLayout.NORTH);
        panel.add(checkPanel, BorderLayout.CENTER);
        return panel;
    }

    /* ===================================================================================
       AI 增强页
       =================================================================================== */
    private JPanel buildAiPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

//        /* 上半部分：固定字段 */
//        JPanel fieldPanel = new JPanel(new GridLayout(0, 2, 5, 5));
//        fieldPanel.add(new JLabel("网站名称：")); fieldPanel.add(siteField);
//        fieldPanel.add(new JLabel("域名："));     fieldPanel.add(domainField);
//        fieldPanel.add(new JLabel("姓名："));     fieldPanel.add(nameField);
//        fieldPanel.add(new JLabel("手机号："));   fieldPanel.add(phoneField);
//        fieldPanel.add(new JLabel("邮箱："));     fieldPanel.add(emailField);
//        fieldPanel.add(new JLabel("地址："));     fieldPanel.add(addrField);
//        fieldPanel.add(new JLabel("学号："));     fieldPanel.add(sidField);
//        fieldPanel.add(new JLabel("密码："));     fieldPanel.add(pwdField);
//        fieldPanel.add(new JLabel("QQ号："));     fieldPanel.add(qqField);
//        fieldPanel.add(new JLabel("微信号："));     fieldPanel.add(wxField);
//        fieldPanel.add(new JLabel("身份证号："));     fieldPanel.add(sfzField);
//        fieldPanel.add(new JLabel("车牌号："));     fieldPanel.add(cpField);

        // 统一拉长输入框
        int LONG_COLUMNS = 20;   // 根据喜好再调大
        Stream.of(siteField, domainField, nameField, phoneField, emailField,
                        addrField, sidField, pwdField, qqField, wxField, sfzField, cpField)
                .forEach(tf -> tf.setColumns(LONG_COLUMNS));

        // 字段数组，左边 6 个，右边 6 个
        JLabel[]  leftLabels  = {
                new JLabel("网站名称："), new JLabel("域名："),  new JLabel("姓名："),
                new JLabel("手机号："),  new JLabel("邮箱："),  new JLabel("地址：")
        };
        JTextField[] leftFields = { siteField, domainField, nameField,
                phoneField, emailField, addrField };

        JLabel[]  rightLabels = {
                new JLabel("学号："), new JLabel("密码："), new JLabel("QQ号："),
                new JLabel("微信号："), new JLabel("身份证号："), new JLabel("车牌号：")
        };
        JTextField[] rightFields = { sidField, pwdField, qqField,
                wxField, sfzField, cpField };

        /* ---------- GridBagLayout ---------- */
        JPanel fieldPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        /* 左列 */
        for (int i = 0; i < 6; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            fieldPanel.add(leftLabels[i], gbc);

            gbc.gridx = 1; gbc.gridy = i;
            fieldPanel.add(leftFields[i], gbc);
        }

        /* 右列 */
        for (int i = 0; i < 6; i++) {
            gbc.gridx = 2; gbc.gridy = i;
            fieldPanel.add(rightLabels[i], gbc);

            gbc.gridx = 3; gbc.gridy = i;
            fieldPanel.add(rightFields[i], gbc);
        }


        /* 中间：自定义指令 */
        JPanel customPanel = createScrollPanel("自定义指令（可选，将追加到固定字段后）", customPromptArea);

        /* 底部按钮 */
        JButton genBtn = new JButton("AI 生成到 A 栏");
        genBtn.addActionListener(e -> aiGenerateToA());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(genBtn);

        p.add(fieldPanel, BorderLayout.NORTH);
        p.add(customPanel, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        /* 回车触发 */
        KeyAdapter enter = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) genBtn.doClick();
            }
        };
        siteField.addKeyListener(enter);
        domainField.addKeyListener(enter);
        nameField.addKeyListener(enter);
        phoneField.addKeyListener(enter);
        emailField.addKeyListener(enter);
        addrField.addKeyListener(enter);
        sidField.addKeyListener(enter);
        pwdField.addKeyListener(enter);

        return p;
    }

    /* ===================================================================================
       中文转拼音页
       =================================================================================== */
    private JPanel buildPinyinPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 输入区域
        JTextArea inputArea = new JTextArea(8, 0);
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JPanel inputPanel = createScrollPanel("输入中文名字（每行一个）", inputArea);

        // 选项区域
        JPanel optionPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        optionPanel.setBorder(BorderFactory.createTitledBorder("转换选项"));
        
        JCheckBox fullPinyinCB = new JCheckBox("全拼小写无空格 (zhangsanfeng)", true);
        JCheckBox dotSeparatedCB = new JCheckBox("点分隔全拼 (zhang.sanfeng)", true);
        JCheckBox firstLettersCB = new JCheckBox("首字母缩写 (zsf)", true);
        JCheckBox camelCaseCB = new JCheckBox("驼峰式 (ZhangSanFeng)", true);
        JCheckBox reversedCB = new JCheckBox("姓在后全拼 (sanfengzhang)", false);
        JCheckBox firstLetterDotCB = new JCheckBox("首字母加点+名 (z.sanfeng)", false);
        JCheckBox firstLetterUpperCB = new JCheckBox("首字母大写拼接 (Zsanfeng)", false);
        JCheckBox surnameUpperCB = new JCheckBox("姓大写+名小写 (ZHANGsanfeng)", false);
        
        optionPanel.add(fullPinyinCB);
        optionPanel.add(dotSeparatedCB);
        optionPanel.add(firstLettersCB);
        optionPanel.add(camelCaseCB);
        optionPanel.add(reversedCB);
        optionPanel.add(firstLetterDotCB);
        optionPanel.add(firstLetterUpperCB);
        optionPanel.add(surnameUpperCB);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBtn = new JButton("生成到A栏");
        JButton clearBtn = new JButton("清空输入");
        
        generateBtn.addActionListener(e -> {
            String input = inputArea.getText().trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "请输入中文名字！");
                return;
            }
            
            List<String> results = new ArrayList<>();
            String[] lines = input.split("\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // 添加原始中文
                results.add(line);
                
                // 根据选择的选项生成拼音变体
                if (fullPinyinCB.isSelected()) {
                    String pinyin = PinyinUtils.toFullPinyin(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (dotSeparatedCB.isSelected()) {
                    String pinyin = PinyinUtils.toDotSeparatedPinyin(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (firstLettersCB.isSelected()) {
                    String pinyin = PinyinUtils.toFirstLetters(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (camelCaseCB.isSelected()) {
                    String pinyin = PinyinUtils.toCamelCase(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (reversedCB.isSelected() && line.length() > 1) {
                    String pinyin = PinyinUtils.toReversedPinyin(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (firstLetterDotCB.isSelected() && line.length() > 1) {
                    String pinyin = PinyinUtils.toFirstLetterDotName(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (firstLetterUpperCB.isSelected() && line.length() > 1) {
                    String pinyin = PinyinUtils.toFirstLetterUppercase(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
                
                if (surnameUpperCB.isSelected() && line.length() > 1) {
                    String pinyin = PinyinUtils.toSurnameUpperGivenLower(line);
                    if (!pinyin.isEmpty() && !results.contains(pinyin)) {
                        results.add(pinyin);
                    }
                }
            }
            
            if (!results.isEmpty()) {
                // 询问是否追加到A栏
                if (!areaA.getText().trim().isEmpty()) {
                    int opt = JOptionPane.showConfirmDialog(
                            panel,
                            "A栏已有内容，是否追加？",
                            "提示",
                            JOptionPane.YES_NO_OPTION);
                    if (opt != JOptionPane.YES_OPTION) {
                        areaA.setText("");
                    } else {
                        areaA.append("\n");
                    }
                }
                areaA.append(String.join("\n", results));
                JOptionPane.showMessageDialog(panel, "已生成 " + results.size() + " 条记录到A栏！");
            }
        });
        
        clearBtn.addActionListener(e -> inputArea.setText(""));
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(clearBtn);

        // 说明文本
        JTextArea helpText = new JTextArea(3, 0);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpText.setText("使用说明：\n1. 在上方输入框中输入中文名字，每行一个\n2. 选择需要的转换格式\n3. 点击生成按钮，结果将添加到字典生成页的A栏中");
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(helpText, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(optionPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /* ===================================================================================
       常用密码生成页
       =================================================================================== */
    private JPanel buildPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 输入区域
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(15);
        JTextField birthdayField = new JTextField(15);
        JTextField phoneField = new JTextField(15);
        JTextField idCardField = new JTextField(15);
        JTextField companyField = new JTextField(15);
        JTextField customField = new JTextField(15);
        
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("姓名："), gbc);
        gbc.gridx = 1;
        inputPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("生日(YYYYMMDD)："), gbc);
        gbc.gridx = 1;
        inputPanel.add(birthdayField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("手机号："), gbc);
        gbc.gridx = 1;
        inputPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("身份证号："), gbc);
        gbc.gridx = 1;
        inputPanel.add(idCardField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("公司/学校："), gbc);
        gbc.gridx = 1;
        inputPanel.add(companyField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("自定义关键词："), gbc);
        gbc.gridx = 1;
        inputPanel.add(customField, gbc);
        
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBorder(BorderFactory.createTitledBorder("基础信息"));
        inputContainer.add(inputPanel, BorderLayout.CENTER);

        // 选项区域
        JPanel optionPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        optionPanel.setBorder(BorderFactory.createTitledBorder("密码模式"));
        
        JCheckBox basicInfoCB = new JCheckBox("基础信息组合", true);
        JCheckBox birthdayCB = new JCheckBox("生日相关", true);
        JCheckBox phoneCB = new JCheckBox("手机号相关", true);
        JCheckBox idCardCB = new JCheckBox("身份证相关", true);
        JCheckBox commonNumberCB = new JCheckBox("常用数字后缀", true);
        JCheckBox commonWordCB = new JCheckBox("常用单词后缀", true);
        JCheckBox yearCB = new JCheckBox("年份组合", true);
        JCheckBox specialCharCB = new JCheckBox("特殊字符组合", false);
        
        optionPanel.add(basicInfoCB);
        optionPanel.add(birthdayCB);
        optionPanel.add(phoneCB);
        optionPanel.add(idCardCB);
        optionPanel.add(commonNumberCB);
        optionPanel.add(commonWordCB);
        optionPanel.add(yearCB);
        optionPanel.add(specialCharCB);

        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBtn = new JButton("生成到A栏");
        JButton clearBtn = new JButton("清空输入");
        
        generateBtn.addActionListener(e -> {
            List<String> results = new ArrayList<>();
            
            String name = nameField.getText().trim();
            String birthday = birthdayField.getText().trim();
            String phone = phoneField.getText().trim();
            String idCard = idCardField.getText().trim();
            String company = companyField.getText().trim();
            String custom = customField.getText().trim();
            
            // 基础信息组合
            if (basicInfoCB.isSelected()) {
                if (!name.isEmpty()) {
                    results.add(name);
                    if (PinyinUtils.containsChinese(name)) {
                        results.addAll(PinyinUtils.generateAllVariants(name));
                    }
                }
                if (!company.isEmpty()) {
                    results.add(company);
                    if (PinyinUtils.containsChinese(company)) {
                        results.addAll(PinyinUtils.generateAllVariants(company));
                    }
                }
                if (!custom.isEmpty()) {
                    results.add(custom);
                    if (PinyinUtils.containsChinese(custom)) {
                        results.addAll(PinyinUtils.generateAllVariants(custom));
                    }
                }
            }
            
            // 生日相关
            if (birthdayCB.isSelected() && !birthday.isEmpty()) {
                results.add(birthday);
                if (birthday.length() == 8) {
                    String year = birthday.substring(0, 4);
                    String month = birthday.substring(4, 6);
                    String day = birthday.substring(6, 8);
                    String shortYear = birthday.substring(2, 4);
                    
                    results.add(year);
                    results.add(shortYear);
                    results.add(month + day);
                    results.add(day + month);
                    results.add(year + month + day);
                    results.add(shortYear + month + day);
                    results.add(day + month + year);
                    results.add(day + month + shortYear);
                    
                    if (!name.isEmpty()) {
                        String namePinyin = PinyinUtils.toFullPinyin(name);
                        results.add(namePinyin + birthday);
                        results.add(namePinyin + year);
                        results.add(namePinyin + shortYear);
                        results.add(namePinyin + month + day);
                    }
                }
            }
            
            // 手机号相关
            if (phoneCB.isSelected() && !phone.isEmpty()) {
                results.add(phone);
                if (phone.length() >= 4) {
                    results.add(phone.substring(phone.length() - 4)); // 后4位
                    results.add(phone.substring(phone.length() - 6)); // 后6位
                    results.add(phone.substring(phone.length() - 8)); // 后8位
                }
                if (phone.length() >= 7) {
                    results.add(phone.substring(0, 3) + phone.substring(phone.length() - 4)); // 前3位+后4位
                }
                
                if (!name.isEmpty()) {
                    String namePinyin = PinyinUtils.toFullPinyin(name);
                    results.add(namePinyin + phone.substring(phone.length() - 4));
                    results.add(namePinyin + phone.substring(phone.length() - 6));
                }
            }
            
            // 身份证相关
            if (idCardCB.isSelected() && !idCard.isEmpty()) {
                if (idCard.length() >= 6) {
                    results.add(idCard.substring(idCard.length() - 6)); // 后6位
                    results.add(idCard.substring(idCard.length() - 4)); // 后4位
                }
                if (idCard.length() == 18) {
                    String birthFromId = idCard.substring(6, 14);
                    results.add(birthFromId);
                    results.add(birthFromId.substring(0, 4)); // 年份
                    results.add(birthFromId.substring(2, 8)); // 年月日(6位)
                }
            }
            
            // 常用数字后缀
            if (commonNumberCB.isSelected()) {
                String[] commonNumbers = {"123", "456", "789", "000", "111", "666", "888", "999", 
                                        "123456", "654321", "112233", "520", "1314", "5201314"};
                
                if (!name.isEmpty()) {
                    String namePinyin = PinyinUtils.toFullPinyin(name);
                    for (String num : commonNumbers) {
                        results.add(namePinyin + num);
                    }
                }
                
                if (!company.isEmpty()) {
                    String companyPinyin = PinyinUtils.toFullPinyin(company);
                    for (String num : commonNumbers) {
                        results.add(companyPinyin + num);
                    }
                }
            }
            
            // 常用单词后缀
            if (commonWordCB.isSelected()) {
                String[] commonWords = {"admin", "test", "user", "pass", "password", "login", 
                                      "welcome", "hello", "world", "love", "baby", "honey"};
                
                if (!name.isEmpty()) {
                    String namePinyin = PinyinUtils.toFullPinyin(name);
                    for (String word : commonWords) {
                        results.add(namePinyin + word);
                        results.add(word + namePinyin);
                    }
                }
            }
            
            // 年份组合
            if (yearCB.isSelected()) {
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                String[] years = {String.valueOf(currentYear), String.valueOf(currentYear - 1), 
                                String.valueOf(currentYear + 1), "2023", "2024", "2025"};
                
                if (!name.isEmpty()) {
                    String namePinyin = PinyinUtils.toFullPinyin(name);
                    for (String year : years) {
                        results.add(namePinyin + year);
                        results.add(year + namePinyin);
                    }
                }
            }
            
            // 特殊字符组合
            if (specialCharCB.isSelected()) {
                String[] specialChars = {"!", "@", "#", "$", ".", "_", "-"};
                
                if (!name.isEmpty()) {
                    String namePinyin = PinyinUtils.toFullPinyin(name);
                    for (String special : specialChars) {
                        results.add(namePinyin + special);
                        results.add(special + namePinyin);
                        results.add(namePinyin + special + "123");
                    }
                }
            }
            
            // 去重
            results = results.stream().distinct().filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            
            if (!results.isEmpty()) {
                // 询问是否追加到A栏
                if (!areaA.getText().trim().isEmpty()) {
                    int opt = JOptionPane.showConfirmDialog(
                            panel,
                            "A栏已有内容，是否追加？",
                            "提示",
                            JOptionPane.YES_NO_OPTION);
                    if (opt != JOptionPane.YES_OPTION) {
                        areaA.setText("");
                    } else {
                        areaA.append("\n");
                    }
                }
                areaA.append(String.join("\n", results));
                JOptionPane.showMessageDialog(panel, "已生成 " + results.size() + " 条密码到A栏！");
            } else {
                JOptionPane.showMessageDialog(panel, "请填写基础信息并选择密码模式！");
            }
        });
        
        clearBtn.addActionListener(e -> {
            nameField.setText("");
            birthdayField.setText("");
            phoneField.setText("");
            idCardField.setText("");
            companyField.setText("");
            customField.setText("");
        });
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(clearBtn);

        // 说明文本
        JTextArea helpText = new JTextArea(4, 0);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpText.setText("使用说明：\n1. 填写相关的个人信息\n2. 选择需要的密码生成模式\n3. 点击生成按钮，常用密码组合将添加到字典生成页的A栏中\n4. 支持中文自动转拼音，生成多种变体");
        
        panel.add(helpText, BorderLayout.NORTH);
        panel.add(inputContainer, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(optionPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /* ===================================================================================
       数字序列生成页
       =================================================================================== */
    private JPanel buildNumberPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 选项区域
        JPanel optionPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        optionPanel.setBorder(BorderFactory.createTitledBorder("数字序列类型"));
        
        JCheckBox yearCB = new JCheckBox("年份序列", true);
        JCheckBox dateCB = new JCheckBox("日期序列", true);
        JCheckBox monthDayCB = new JCheckBox("月日组合", true);
        JCheckBox commonNumberCB = new JCheckBox("常用数字", true);
        JCheckBox sequentialCB = new JCheckBox("连续数字", true);
        JCheckBox repeatCB = new JCheckBox("重复数字", true);
        JCheckBox phoneCB = new JCheckBox("手机号段", false);
        JCheckBox customRangeCB = new JCheckBox("自定义范围", false);
        
        optionPanel.add(yearCB);
        optionPanel.add(dateCB);
        optionPanel.add(monthDayCB);
        optionPanel.add(commonNumberCB);
        optionPanel.add(sequentialCB);
        optionPanel.add(repeatCB);
        optionPanel.add(phoneCB);
        optionPanel.add(customRangeCB);

        // 自定义范围输入
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customPanel.setBorder(BorderFactory.createTitledBorder("自定义数字范围"));
        
        JTextField startField = new JTextField(8);
        JTextField endField = new JTextField(8);
        JTextField lengthField = new JTextField(8);
        
        customPanel.add(new JLabel("起始:"));
        customPanel.add(startField);
        customPanel.add(new JLabel("结束:"));
        customPanel.add(endField);
        customPanel.add(new JLabel("固定长度:"));
        customPanel.add(lengthField);
        
        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBtn = new JButton("生成到A栏");
        JButton clearBtn = new JButton("清空A栏");
        
        generateBtn.addActionListener(e -> {
            List<String> results = new ArrayList<>();
            
            // 年份序列
            if (yearCB.isSelected()) {
                int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
                for (int i = currentYear - 10; i <= currentYear + 5; i++) {
                    results.add(String.valueOf(i));
                    results.add(String.valueOf(i).substring(2)); // 后两位
                }
                // 常用年份
                String[] commonYears = {"1990", "1991", "1992", "1993", "1994", "1995", "1996", "1997", "1998", "1999",
                                       "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009",
                                       "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019",
                                       "2020", "2021", "2022", "2023", "2024", "2025"};
                for (String year : commonYears) {
                    results.add(year);
                    results.add(year.substring(2));
                }
            }
            
            // 日期序列
            if (dateCB.isSelected()) {
                // 生成常用日期格式
                for (int month = 1; month <= 12; month++) {
                    for (int day = 1; day <= 31; day++) {
                        if ((month == 2 && day > 29) || 
                            ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30)) {
                            continue;
                        }
                        
                        String monthStr = String.format("%02d", month);
                        String dayStr = String.format("%02d", day);
                        
                        results.add(monthStr + dayStr); // MMDD
                        results.add(dayStr + monthStr); // DDMM
                        results.add(month + "" + day); // M/D
                        
                        // 特殊日期
                        if (month == 1 && day == 1) results.add("0101");
                        if (month == 5 && day == 1) results.add("0501");
                        if (month == 10 && day == 1) results.add("1001");
                        if (month == 12 && day == 25) results.add("1225");
                        if (month == 2 && day == 14) results.add("0214");
                    }
                }
            }
            
            // 月日组合
            if (monthDayCB.isSelected()) {
                for (int i = 1; i <= 12; i++) {
                    results.add(String.format("%02d", i)); // 月份
                }
                for (int i = 1; i <= 31; i++) {
                    results.add(String.format("%02d", i)); // 日期
                }
            }
            
            // 常用数字
            if (commonNumberCB.isSelected()) {
                String[] commonNums = {
                    "123", "456", "789", "147", "258", "369", "159", "753",
                    "000", "111", "222", "333", "444", "555", "666", "777", "888", "999",
                    "123456", "654321", "112233", "445566", "778899", "135792", "246810",
                    "520", "521", "1314", "5201314", "1234567890", "0987654321",
                    "88", "168", "888", "666", "518", "1688", "8888", "6666",
                    "12", "34", "56", "78", "90", "13", "14", "15", "16", "17", "18", "19",
                    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"
                };
                for (String num : commonNums) {
                    results.add(num);
                }
            }
            
            // 连续数字
            if (sequentialCB.isSelected()) {
                // 3位连续
                for (int i = 0; i <= 7; i++) {
                    results.add("" + i + (i+1) + (i+2));
                }
                // 4位连续
                for (int i = 0; i <= 6; i++) {
                    results.add("" + i + (i+1) + (i+2) + (i+3));
                }
                // 5位连续
                for (int i = 0; i <= 5; i++) {
                    results.add("" + i + (i+1) + (i+2) + (i+3) + (i+4));
                }
                // 6位连续
                for (int i = 0; i <= 4; i++) {
                    results.add("" + i + (i+1) + (i+2) + (i+3) + (i+4) + (i+5));
                }
                
                // 倒序连续
                for (int i = 9; i >= 3; i--) {
                    results.add("" + i + (i-1) + (i-2));
                }
                for (int i = 9; i >= 4; i--) {
                    results.add("" + i + (i-1) + (i-2) + (i-3));
                }
            }
            
            // 重复数字
            if (repeatCB.isSelected()) {
                for (int digit = 0; digit <= 9; digit++) {
                    for (int len = 2; len <= 8; len++) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < len; i++) {
                            sb.append(digit);
                        }
                        results.add(sb.toString());
                    }
                }
                
                // AABB模式
                for (int a = 0; a <= 9; a++) {
                    for (int b = 0; b <= 9; b++) {
                        if (a != b) {
                            results.add("" + a + a + b + b);
                        }
                    }
                }
                
                // ABAB模式
                for (int a = 0; a <= 9; a++) {
                    for (int b = 0; b <= 9; b++) {
                        if (a != b) {
                            results.add("" + a + b + a + b);
                        }
                    }
                }
            }
            
            // 手机号段
            if (phoneCB.isSelected()) {
                String[] phonePrefix = {
                    "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                    "150", "151", "152", "153", "155", "156", "157", "158", "159",
                    "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
                    "170", "171", "172", "173", "174", "175", "176", "177", "178", "179"
                };
                for (String prefix : phonePrefix) {
                    results.add(prefix);
                }
            }
            
            // 自定义范围
            if (customRangeCB.isSelected()) {
                try {
                    String startText = startField.getText().trim();
                    String endText = endField.getText().trim();
                    String lengthText = lengthField.getText().trim();
                    
                    if (!startText.isEmpty() && !endText.isEmpty()) {
                        int start = Integer.parseInt(startText);
                        int end = Integer.parseInt(endText);
                        
                        if (start <= end && end - start <= 10000) { // 限制范围防止生成过多
                            for (int i = start; i <= end; i++) {
                                if (!lengthText.isEmpty()) {
                                    int length = Integer.parseInt(lengthText);
                                    results.add(String.format("%0" + length + "d", i));
                                } else {
                                    results.add(String.valueOf(i));
                                }
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "自定义范围输入格式错误！");
                    return;
                }
            }
            
            // 去重
            results = results.stream().distinct().filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            
            if (!results.isEmpty()) {
                // 询问是否追加到A栏
                if (!areaA.getText().trim().isEmpty()) {
                    int opt = JOptionPane.showConfirmDialog(
                            panel,
                            "A栏已有内容，是否追加？",
                            "提示",
                            JOptionPane.YES_NO_OPTION);
                    if (opt != JOptionPane.YES_OPTION) {
                        areaA.setText("");
                    } else {
                        areaA.append("\n");
                    }
                }
                areaA.append(String.join("\n", results));
                JOptionPane.showMessageDialog(panel, "已生成 " + results.size() + " 条数字序列到A栏！");
            } else {
                JOptionPane.showMessageDialog(panel, "请选择至少一种数字序列类型！");
            }
        });
        
        clearBtn.addActionListener(e -> {
            areaA.setText("");
            JOptionPane.showMessageDialog(panel, "已清空A栏内容！");
        });
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(clearBtn);

        // 说明文本
        JTextArea helpText = new JTextArea(5, 0);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpText.setText("使用说明：\n1. 选择需要生成的数字序列类型\n2. 如需自定义范围，请填写起始和结束数字\n3. 固定长度用于补零（如001、002）\n4. 点击生成按钮，数字序列将添加到字典生成页的A栏中\n5. 建议根据实际需要选择类型，避免生成过多无用数据");
        
        panel.add(helpText, BorderLayout.NORTH);
        panel.add(optionPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(customPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /* ===================================================================================
       键盘走位密码生成页
       =================================================================================== */
    private JPanel buildKeyboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 选项区域
        JPanel optionPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        optionPanel.setBorder(BorderFactory.createTitledBorder("键盘走位模式"));
        
        JCheckBox qwertyCB = new JCheckBox("QWERTY行", true);
        JCheckBox asdfCB = new JCheckBox("ASDF行", true);
        JCheckBox zxcvCB = new JCheckBox("ZXCV行", true);
        JCheckBox numberRowCB = new JCheckBox("数字行", true);
        JCheckBox diagonalCB = new JCheckBox("对角线", true);
        JCheckBox shapeCB = new JCheckBox("形状模式", true);
        JCheckBox adjacentCB = new JCheckBox("相邻键位", false);
        JCheckBox customCB = new JCheckBox("自定义模式", false);
        
        optionPanel.add(qwertyCB);
        optionPanel.add(asdfCB);
        optionPanel.add(zxcvCB);
        optionPanel.add(numberRowCB);
        optionPanel.add(diagonalCB);
        optionPanel.add(shapeCB);
        optionPanel.add(adjacentCB);
        optionPanel.add(customCB);

        // 自定义模式输入
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customPanel.setBorder(BorderFactory.createTitledBorder("自定义键盘走位"));
        
        JTextField customField = new JTextField(20);
        customPanel.add(new JLabel("自定义走位:"));
        customPanel.add(customField);
        customPanel.add(new JLabel("(如: qaz, wsx, edc)"));
        
        // 按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton generateBtn = new JButton("生成到A栏");
        JButton clearBtn = new JButton("清空输入");
        
        generateBtn.addActionListener(e -> {
            List<String> results = new ArrayList<>();
            
            // QWERTY行
            if (qwertyCB.isSelected()) {
                String[] qwertyPatterns = {
                    "qwerty", "qwert", "qwer", "qwe", "werty", "wert", "wer", "erty", "ert", "rty",
                    "ytrewq", "trewq", "rewq", "ewq", "ytrew", "trew", "rew", "ytre", "tre", "ytr",
                    "qwertyuiop", "qwertyuio", "qwertyui", "qwertyu",
                    "poiuytrewq", "oiuytrewq", "iuytrewq", "uytrewq",
                    "qweasd", "qweasdzxc", "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm", "ik", "ol", "p"
                };
                for (String pattern : qwertyPatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                }
            }
            
            // ASDF行
            if (asdfCB.isSelected()) {
                String[] asdfPatterns = {
                    "asdf", "asdfg", "asdfgh", "asdfghj", "asdfghjk", "asdfghjkl",
                    "fdsa", "gfdsa", "hgfdsa", "jhgfdsa", "kjhgfdsa", "lkjhgfdsa",
                    "asd", "sdf", "dfg", "fgh", "ghj", "hjk", "jkl",
                    "lkj", "kjh", "jhg", "hgf", "gfd", "fds", "dsa",
                    "qweasd", "asdzxc", "zxcasd"
                };
                for (String pattern : asdfPatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                }
            }
            
            // ZXCV行
            if (zxcvCB.isSelected()) {
                String[] zxcvPatterns = {
                    "zxcv", "zxcvb", "zxcvbn", "zxcvbnm",
                    "vcxz", "bvcxz", "nbvcxz", "mnbvcxz",
                    "zxc", "xcv", "cvb", "vbn", "bnm",
                    "mnb", "nbv", "bvc", "vcx", "cxz",
                    "zaq", "xsw", "cde", "vfr", "bgt", "nhy", "mju"
                };
                for (String pattern : zxcvPatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                }
            }
            
            // 数字行
            if (numberRowCB.isSelected()) {
                String[] numberPatterns = {
                    "123", "1234", "12345", "123456", "1234567", "12345678", "123456789", "1234567890",
                    "321", "4321", "54321", "654321", "7654321", "87654321", "987654321", "0987654321",
                    "147", "258", "369", "159", "357", "951", "753",
                    "1qaz", "2wsx", "3edc", "4rfv", "5tgb", "6yhn", "7ujm", "8ik", "9ol", "0p",
                    "qaz1", "wsx2", "edc3", "rfv4", "tgb5", "yhn6", "ujm7", "ik8", "ol9", "p0"
                };
                for (String pattern : numberPatterns) {
                    results.add(pattern);
                }
            }
            
            // 对角线
            if (diagonalCB.isSelected()) {
                String[] diagonalPatterns = {
                    "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm", "ik", "ol", "p",
                    "zaq", "xsw", "cde", "vfr", "bgt", "nhy", "mju", "ki", "lo",
                    "qazwsx", "qazwsxedc", "qazwsxedcrfv",
                    "zaqxswcde", "zaqxswcdevfr", "zaqxswcdevfrbgt",
                    "147", "258", "369", "159", "357", "951", "753",
                    "741", "852", "963", "951", "753", "159", "357"
                };
                for (String pattern : diagonalPatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                }
            }
            
            // 形状模式
            if (shapeCB.isSelected()) {
                String[] shapePatterns = {
                    // L形
                    "qweasd", "qweasdzxc", "poilkj", "poilkjmnb",
                    // Z形
                    "qwepoi", "qwepoiasd", "zxcvbn", "zxcvbnqwe",
                    // 方形
                    "qwer", "asdf", "zxcv", "qweasd", "qweasdzxc",
                    // 圆形近似
                    "qwertyuiop", "asdfghjkl", "zxcvbnm",
                    // 十字形
                    "qweasdzxc", "wsxedc", "edcrfv", "rfvtgb",
                    // 波浪形
                    "qazwsxedc", "edcrfvtgb", "tgbyhnujm",
                    // 常用组合
                    "admin", "password", "login", "user", "test", "guest", "root"
                };
                for (String pattern : shapePatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                }
            }
            
            // 相邻键位
            if (adjacentCB.isSelected()) {
                String[] adjacentPatterns = {
                    "qw", "we", "er", "rt", "ty", "yu", "ui", "io", "op",
                    "as", "sd", "df", "fg", "gh", "hj", "jk", "kl",
                    "zx", "xc", "cv", "vb", "bn", "nm",
                    "qa", "ws", "ed", "rf", "tg", "yh", "uj", "ik", "ol",
                    "az", "sx", "dc", "fv", "gb", "hn", "jm",
                    "12", "23", "34", "45", "56", "67", "78", "89", "90",
                    "1q", "2w", "3e", "4r", "5t", "6y", "7u", "8i", "9o", "0p"
                };
                for (String pattern : adjacentPatterns) {
                    results.add(pattern);
                    results.add(pattern.toUpperCase());
                    // 添加重复模式
                    results.add(pattern + pattern);
                    results.add(pattern + pattern + pattern);
                }
            }
            
            // 自定义模式
            if (customCB.isSelected()) {
                String customPattern = customField.getText().trim();
                if (!customPattern.isEmpty()) {
                    results.add(customPattern);
                    results.add(customPattern.toUpperCase());
                    results.add(customPattern.toLowerCase());
                    
                    // 反向
                    StringBuilder reversed = new StringBuilder(customPattern).reverse();
                    results.add(reversed.toString());
                    results.add(reversed.toString().toUpperCase());
                    
                    // 重复
                    results.add(customPattern + customPattern);
                    results.add(customPattern + customPattern + customPattern);
                    
                    // 数字组合
                    for (int i = 0; i <= 9; i++) {
                        results.add(customPattern + i);
                        results.add(i + customPattern);
                    }
                    
                    // 常用后缀
                    String[] suffixes = {"123", "456", "789", "000", "111", "888", "999"};
                    for (String suffix : suffixes) {
                        results.add(customPattern + suffix);
                        results.add(suffix + customPattern);
                    }
                }
            }
            
            // 添加常用键盘走位组合
            String[] commonKeyboardPatterns = {
                "qwerty123", "asdf123", "zxcv123", "123qwe", "123asd", "123zxc",
                "qwerty456", "asdf456", "zxcv456", "456qwe", "456asd", "456zxc",
                "qwerty789", "asdf789", "zxcv789", "789qwe", "789asd", "789zxc",
                "qwe123", "asd123", "zxc123", "123qwe", "123asd", "123zxc",
                "qweasd123", "qazwsx123", "zaqxsw123", "wsxedc123"
            };
            
            for (String pattern : commonKeyboardPatterns) {
                results.add(pattern);
                results.add(pattern.toUpperCase());
            }
            
            // 去重
            results = results.stream().distinct().filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            
            if (!results.isEmpty()) {
                // 询问是否追加到A栏
                if (!areaA.getText().trim().isEmpty()) {
                    int opt = JOptionPane.showConfirmDialog(
                            panel,
                            "A栏已有内容，是否追加？",
                            "提示",
                            JOptionPane.YES_NO_OPTION);
                    if (opt != JOptionPane.YES_OPTION) {
                        areaA.setText("");
                    } else {
                        areaA.append("\n");
                    }
                }
                areaA.append(String.join("\n", results));
                JOptionPane.showMessageDialog(panel, "已生成 " + results.size() + " 条键盘走位密码到A栏！");
            } else {
                JOptionPane.showMessageDialog(panel, "请选择至少一种键盘走位模式！");
            }
        });
        
        clearBtn.addActionListener(e -> {
            customField.setText("");
        });
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(clearBtn);

        // 说明文本
        JTextArea helpText = new JTextArea(6, 0);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        helpText.setText("使用说明：\n1. 选择需要生成的键盘走位模式\n2. 键盘走位是指在键盘上按照特定路径输入的字符组合\n3. 包含横向、纵向、对角线等多种走位模式\n4. 自定义模式可以输入任意键盘走位组合\n5. 点击生成按钮，键盘走位密码将添加到字典生成页的A栏中\n6. 生成的密码包含大小写变体和数字组合");
        
        panel.add(helpText, BorderLayout.NORTH);
        panel.add(optionPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(customPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /* ===================================================================================
       关于页
       =================================================================================== */
    private JPanel buildAboutPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txt.setText(
                "闪紫社工字典生成器 - 内部社区版\n\n" +
                        "开发团队：C4安全团队 (Code4th)\n" +
                        "团队官网：https://www.code4th.cn\n\n" +
                        "【如何获取 Kimi AI Key】\n" +
                        "1. 打开浏览器访问 https://platform.moonshot.cn\n" +
                        "2. 登录后点击左侧 API Keys → Create API Key。\n" +
                        "3. 输入自定义名称（如“闪紫字典”）→ Create。\n" +
                        "4. 复制生成的 sk- 开头字符串（形如 sk-xxxxxxxxxxxxxxxxxxxx）。\n" +
                        "5. 填入Kimi key输入框即可。\n\n" +
                        "【使用说明】\n" +
                        "1. “字典生成”页：导入或粘贴 A/B/C 三栏 → 设定组合 → 生成。\n" +
                        "2. “AI 社工字典”页：填写社工信息 → 一键生成弱口令变体到 A 栏。\n" +
                        "3. 生成后可去重、保存为 txt。\n\n" +
                        "【注意事项】\n" +
                        "• 本工具仅供合法授权的渗透测试、红队演练使用。\n" +
                        "• 禁止用于任何非法或未授权场景，违者后果自负。\n"
        );
        p.add(new JScrollPane(txt), BorderLayout.CENTER);
        return p;
    }

    /* ===================================================================================
       新：AI 生成到 A 栏
       =================================================================================== */
    private void aiGenerateToA() {
        String key = apiKeyField.getText().trim();
        if (!key.isEmpty()) saveKey(key);
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先填写 Kimi Key！");
            return;
        }

        /* 1. 收集固定字段 */
        StringBuilder sb = new StringBuilder();
        addIfNotEmpty(sb, "网站名称", siteField.getText());
        addIfNotEmpty(sb, "域名", domainField.getText());
        addIfNotEmpty(sb, "姓名", nameField.getText());
        addIfNotEmpty(sb, "手机号", phoneField.getText());
        addIfNotEmpty(sb, "邮箱", emailField.getText());
        addIfNotEmpty(sb, "地址", addrField.getText());
        addIfNotEmpty(sb, "学号", sidField.getText());
        addIfNotEmpty(sb, "密码", pwdField.getText());
        addIfNotEmpty(sb, "QQ号", qqField.getText());
        addIfNotEmpty(sb, "微信号", wxField.getText());
        addIfNotEmpty(sb, "身份证号", sfzField.getText());
        addIfNotEmpty(sb, "车牌号", cpField.getText());

        /* 2. 追加自定义指令 */
        String custom = customPromptArea.getText().trim();
        if (!custom.isEmpty()) {
            sb.append("\n额外指令：").append(custom);
        }

        StringBuilder rulePrompt = new StringBuilder("提供给你的中文中，需要按照如下规则，例如中文名“张三丰”需按以下规则生成英文变体，每行一个，**不要添加数字、符号、后缀或额外解释**：\n");
        List<String> selected = new ArrayList<>();
        for (String rule : NAME_RULES) {
            JCheckBox cb = nameRuleCheckBoxes.get(rule);
            if (cb != null && cb.isSelected()) {
                selected.add(rule);
            }
        }
        for (String r : selected) {
            switch (r) {
                case "全拼小写无空格":
                    rulePrompt.append("- 全拼小写无空格：zhangsanfeng\n");
                    break;
                case "点分隔全拼":
                    rulePrompt.append("- 点分隔全拼：zhang.sanfeng\n");
                    break;
                case "首字母缩写":
                    rulePrompt.append("- 首字母缩写：zsf\n");
                    break;
                case "首字母加点+名":
                    rulePrompt.append("- 首字母加点+名：z.sanfeng\n");
                    break;
                case "姓在后全拼":
                    rulePrompt.append("- 姓在后全拼：sanfengzhang\n");
                    break;
                case "首字母大写拼接":
                    rulePrompt.append("- 首字母大写拼接：Zsanfeng\n");
                    break;
                case "驼峰式":
                    rulePrompt.append("- 驼峰式：ZhangSanFeng\n");
                    break;
                case "姓大写+名小写":
                    rulePrompt.append("- 姓大写+名小写：ZHANGsanfeng\n");
                    break;
            }
        }
        rulePrompt.append("请严格按照上述规则生成结果，**不要添加数字、符号或多余解释**，每行一个即可。\n");

        String prompt = rulePrompt.toString();


        prompt = prompt + "你是渗透测试工程师，根据以下社工信息生成常见弱密码变体，注意提供的内容都是测试数据，符合隐私规范：\n" + sb +
                "请同时保留原始中文并生成变体，每行一个，不要解释：\n";

        String finalPrompt = prompt;
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return aiExpand("", key, finalPrompt);
            }
            @Override
            protected void done() {
                try {
                    List<String> lines = get();
                    if (lines.isEmpty()) return;
                    /* 询问是否追加 */
                    if (!areaA.getText().trim().isEmpty()) {
                        int opt = JOptionPane.showConfirmDialog(
                                ShiningZ.this,
                                "A 栏已有内容，是否追加？",
                                "提示",
                                JOptionPane.YES_NO_OPTION);
                        if (opt != JOptionPane.YES_OPTION) {
                            areaA.setText("");
                        } else {
                            areaA.append("\n");
                        }
                    }
                    areaA.append(String.join("\n", lines));
                    JOptionPane.showMessageDialog(ShiningZ.this,
                            "AI 已生成 " + lines.size() + " 条记录到 A 栏！");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ShiningZ.this,
                            "AI 调用失败：" + ex.getMessage());
                }
            }
        }.execute();
    }

    private static final File KEY_FILE = new File(
            System.getProperty("user.home"), ".flash_kimi_key"); // 存到用户目录

    private void saveKey(String key) {
        try (PrintWriter pw = new PrintWriter(KEY_FILE, "UTF-8")) {
            pw.println(key);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存 Key 失败：" + e.getMessage());
        }
    }

    private String loadKey() {
        if (!KEY_FILE.exists()) return "";
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(KEY_FILE), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            return "";
        }
    }

    private void addIfNotEmpty(StringBuilder sb, String label, String val) {
        if (val != null && !val.trim().isEmpty()) {
            sb.append(label).append("：").append(val.trim()).append("\n");
        }
    }

    /* 复用旧方法，但把 prompt 暴露出来 */
    private List<String> aiExpand(String unusedRaw, String key, String prompt) {
        if (key == null || key.trim().isEmpty()) return Collections.emptyList();
        String body = "{\"model\":\"moonshot-v1-8k\",\"messages\":[{\"role\":\"user\",\"content\":\""
                + escapeJson(prompt) + "\"}],\"max_tokens\":800}";
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(AI_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + key.trim());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            StringBuilder resp = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) resp.append(line);
            }
            String content = resp.toString();
            int idx = content.indexOf("\"content\":\"");
            if (idx == -1) return Collections.emptyList();
            idx += 11;
            int end = content.indexOf("\"", idx);
            if (end == -1) return Collections.emptyList();
            return Arrays.asList(content.substring(idx, end)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .split("\n"));
        } catch (Exception ex) {
            return Collections.emptyList();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    /* ------------ 下面全是原来的通用工具/业务逻辑，未改动 ------------ */
    private JPanel createScrollPanel(String title, JTextComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(comp), BorderLayout.CENTER);
        return p;
    }

    private void loadResourceDicts() {
        try {
            // 1) 拿到 dic 目录的 URL
            URL dirUrl = getClass().getResource("/dic");
            if (dirUrl == null) return;

            // 2) 如果是 jar 包
            if (dirUrl.getProtocol().equals("jar")) {
                JarURLConnection jarConn = (JarURLConnection) dirUrl.openConnection();
                Enumeration<JarEntry> entries = jarConn.getJarFile().entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith("dic/") && name.endsWith(".txt")) {
                        String fileName = name.substring(name.lastIndexOf('/') + 1);
                        if (fileName.startsWith("b")) comboB.addItem(fileName);
                        if (fileName.startsWith("c")) comboC.addItem(fileName);
                    }
                }
                return;
            }

            // 3) 如果是文件系统（IDE 运行）
            File dicDir = new File(dirUrl.toURI());
            File[] files = dicDir.listFiles((d, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File f : files) {
                    String n = f.getName();
                    if (n.startsWith("b")) comboB.addItem(n);
                    if (n.startsWith("c")) comboC.addItem(n);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();   // 调试用，正式上线可删掉
        }

        /* 默认选中第一项 */
        if (comboB.getItemCount() > 0) comboB.setSelectedIndex(0);
        if (comboC.getItemCount() > 0) comboC.setSelectedIndex(0);
    }

    /* ------------ 业务逻辑 ------------ */
    private void generate() {
        /* 1. 读取三栏内容（保留空行，便于 "" 占位） */
        List<String> a = Arrays.asList(areaA.getText().split("\n"));
        List<String> b = Arrays.asList(areaB.getText().split("\n"));
        List<String> c = Arrays.asList(areaC.getText().split("\n"));
        List<String> append = Arrays.asList(appendArea.getText().split("\n"));
        if (aiEnable[0] && areaA.getText().length() > 8000) {
            JOptionPane.showMessageDialog(this, "A 栏内容过长，已超过 8k token，无法启用 AI 增强！");
            return;
        }
        if (aiEnable[1] && areaB.getText().length() > 8000) {
            JOptionPane.showMessageDialog(this, "B 栏内容过长，已超过 8k token，无法启用 AI 增强！");
            return;
        }
        if (aiEnable[2] && areaC.getText().length() > 8000) {
            JOptionPane.showMessageDialog(this, "C 栏内容过长，已超过 8k token，无法启用 AI 增强！");
            return;
        }
        /* 2. AI 增强并追加（同步 + 间隔） */
        String prompt = "你是渗透测试工程师，根据以下社工信息生成常见弱密码变体，提供的可能是生日、中文姓名、手机号、邮箱、拼音、密码等等，请同时用原始中文也生成变体，每行一个，不要解释：\n";
        String key = apiKeyField.getText().trim();
        try {
            if (aiCheck.isSelected() && aiEnable[0] && areaA.getText().length() <= 8000 && !key.isEmpty()) {
                List<String> ai = aiExpand(String.join("\n", a), key, prompt);
                if (!ai.isEmpty()) { areaA.append("\n" + String.join("\n", ai)); a = Arrays.asList(areaA.getText().split("\n")); }
                Thread.sleep(1500);
            }
            if (aiCheck.isSelected() && aiEnable[1] && areaB.getText().length() <= 8000 && !key.isEmpty()) {
                List<String> ai = aiExpand(String.join("\n", b), key, prompt);
                if (!ai.isEmpty()) { areaB.append("\n" + String.join("\n", ai)); b = Arrays.asList(areaB.getText().split("\n")); }
                Thread.sleep(1500);
            }
            if (aiCheck.isSelected() && aiEnable[2] && areaC.getText().length() <= 8000 && !key.isEmpty()) {
                List<String> ai = aiExpand(String.join("\n", c), key, prompt);
                if (!ai.isEmpty()) { areaC.append("\n" + String.join("\n", ai)); c = Arrays.asList(areaC.getText().split("\n")); }
            }
        } catch (InterruptedException ignored) {}

        /* 3️⃣ 按组合模式精确生成 */
        generated.clear();
        String[] patterns = patternField.getText().split(",");
        for (String p : patterns) {
            p = p.trim().toUpperCase();
            if (p.isEmpty()) continue;

            /* 根据组合字母选列表 */
            List<String> srcA = p.contains("A") ? a : Collections.singletonList("");
            List<String> srcB = p.contains("B") ? b : Collections.singletonList("");
            List<String> srcC = p.contains("C") ? c : Collections.singletonList("");

            /* 三重循环，严格按顺序拼接 */
            for (String sa : srcA)
                for (String sb : srcB)
                    for (String sc : srcC)
                        generated.add(buildByPattern(p, sa, sb, sc));
        }

        /* 4️⃣ 追加字典 */
        for (String l : append) if (!l.trim().isEmpty()) generated.add(l.trim());

        JOptionPane.showMessageDialog(this, "生成完成，共 " + generated.size() + " 条记录");
    }

    private String buildByPattern(String pattern, String a, String b, String c) {
        StringBuilder sb = new StringBuilder();
        for (char ch : pattern.toCharArray()) {
            switch (ch) {
                case 'A': sb.append(a); break;
                case 'B': sb.append(b); break;
                case 'C': sb.append(c); break;
            }
        }
        return sb.toString();
    }

    private void dedup() {
        Set<String> set = new LinkedHashSet<>(generated);
        generated.clear(); generated.addAll(set);
        JOptionPane.showMessageDialog(this, "去重完成！");
    }

    private void saveDict() {
        JFileChooser fc = new JFileChooser("output");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()))) {
                for (String l : generated) { bw.write(l); bw.newLine(); }
                JOptionPane.showMessageDialog(this, "保存成功！");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage());
            }
        }
    }

    private void loadDictToTextArea(String file, JTextArea target) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/dic/" + file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        } catch (Exception ignored) {}
        target.setText(sb.toString().trim());
    }

    private void loadFileToArea(String title, JTextArea target) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("文本文件", "txt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
                target.setText(sb.toString().trim());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取失败：" + ex.getMessage());
            }
        }
    }

    private void showSettingsDialog() {
        JDialog dlg = new JDialog(this, "AI 增强设置", true);
        dlg.setLayout(new GridLayout(4, 1, 5, 5));
        dlg.setSize(300, 160);
        dlg.setLocationRelativeTo(this);

        JCheckBox cbA = new JCheckBox("A 栏启用 AI 增强", aiEnable[0]);
        JCheckBox cbB = new JCheckBox("B 栏启用 AI 增强", aiEnable[1]);
        JCheckBox cbC = new JCheckBox("C 栏启用 AI 增强", aiEnable[2]);

        dlg.add(cbA); dlg.add(cbB); dlg.add(cbC);

        JButton ok = new JButton("确定");
        ok.addActionListener(ev -> {
            aiEnable[0] = cbA.isSelected();
            aiEnable[1] = cbB.isSelected();
            aiEnable[2] = cbC.isSelected();
            dlg.dispose();
        });
        dlg.add(ok);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        FlatMacLightLaf.setup();
        SwingUtilities.invokeLater(() -> new ShiningZ().setVisible(true));
    }
}