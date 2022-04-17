package com.caojx.idea.plugin.ui;

import com.caojx.idea.plugin.common.constants.Constant;
import com.caojx.idea.plugin.common.context.GeneratorContext;
import com.caojx.idea.plugin.common.enums.FrameworkTypeEnum;
import com.caojx.idea.plugin.common.pojo.model.Database;
import com.caojx.idea.plugin.common.pojo.model.TableInfo;
import com.caojx.idea.plugin.common.pojo.persistent.PersistentState;
import com.caojx.idea.plugin.common.properties.*;
import com.caojx.idea.plugin.common.utils.MySQLDBHelper;
import com.caojx.idea.plugin.generator.GeneratorServiceImpl;
import com.caojx.idea.plugin.generator.IGeneratorService;
import com.caojx.idea.plugin.generator.PersistentStateService;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 代码生成配置UI
 *
 * @author caojx
 * @date 2022/4/10 10:00 AM
 */
public class GeneratorSettingUI extends DialogWrapper {

    private JTabbedPane tabbedPanel;
    private JTextField projectPathTf;
    private JTextField entityPathTf;
    private JTextField entityPackageTf;
    private JTextField mapperPathTf;
    private JTextField mapperPackageTf;
    private JTextField mapperXmlPathTf;
    private JButton entityPathBtn;
    private JButton mapperPathBtn;
    private JButton mapperXmlPathBtn;
    private JTextField entityNamePatternTf;
    private JCheckBox dataCheckBox;
    private JCheckBox builderCheckBox;
    private JCheckBox noArgsConstructorCheckBox;
    private JCheckBox allArgsConstructorCheckBox;
    private JTextField mapperNamePatternTf;
    private JTextField superMapperClassTf;
    private JCheckBox enableInsertCheckBox;
    private JCheckBox enableSelectByPrimaryKeyCheckBox;
    private JCheckBox enableSelectByExampleCheckBox;
    private JCheckBox enableUpdateByPrimaryKeyCheckBox;
    private JCheckBox enableUpdateByExampleCheckBox;
    private JCheckBox enableDeleteByPrimaryKeyCheckBox;
    private JCheckBox enableDeleteByExampleCheckBox;
    private JCheckBox enableCountByExampleCheckBox;
    private JTextField mapperXmlNamePatternTf;
    private JTextField servicePathTf;
    private JTextField servicePackageTf;
    private JTextField serviceNamePatternTf;
    private JTextField superServiceClassTf;
    private JButton servicePathBtn;
    private JTextField serviceImplPathTf;
    private JTextField serviceImplPackageTf;
    private JTextField serviceImplNamePatternTf;
    private JTextField superServiceImplClassTf;
    private JTextField controllerPathTf;
    private JTextField controllerPackageTf;
    private JTextField controllerNamePatternTf;
    private JCheckBox controllerSwaggerCheckBox;
    private JComboBox databaseComboBox;
    private JTextField tableNameRegexTf;
    private JButton selectTableBtn;
    private JButton configDataBaseBtn;
    private JTable table;
    private JComboBox frameworkTypeComboBox;
    private JButton serviceImplPathBtn;
    private JButton controllerPathBtn;
    private JPanel mainPanel;
    private JCheckBox entityGenerateCheckBox;
    private JCheckBox mapperGenerateCheckBox;
    private JCheckBox mapperXmlGenerateCheckBox;
    private JCheckBox serviceGenerateCheckBox;
    private JCheckBox serviceImplGenerateCheckBox;
    private JCheckBox controllerGenerateCheckBox;
    private JTextField authorTf;
    private JCheckBox serializableCheckBox;

    private JButton restConfigBtn;
    private JButton saveConfigBtn;
    private JButton generatorBtn;
    private JButton cancelBtn;


    /**
     * 项目
     */
    private Project project;

    /**
     * 数据库列表
     */
    private List<Database> databases = new ArrayList<>();

    /**
     * 选择的数据库
     */
    private Database selectedDatabase;

    /**
     * 选中的表名列表
     */
    private List<String> selectedTableNames = new ArrayList<>();

    /**
     * 生成代码业务接口
     */
    private IGeneratorService generatorService = new GeneratorServiceImpl();

    public GeneratorSettingUI(Project project) {
        super(true);
        init();
        this.project = project;

        // 初始化界面数据
        renderUIData(project);

        // 创建事件监听器
        initActionListener(project);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    @Override
    protected JComponent createSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout());
        // 重置配置
        restConfigBtn = new JButton("重置配置");
        southPanel.add(restConfigBtn);

        // 保存配置
        saveConfigBtn = new JButton("保存配置");
        southPanel.add(saveConfigBtn);

        // 生成代码
        generatorBtn = new JButton("生成代码");
        southPanel.add(generatorBtn);

        // 取消配置
        cancelBtn = new JButton("取消");
        southPanel.add(cancelBtn);
        return southPanel;
    }

    /**
     * 渲染UI数据
     *
     * @param project 项目
     */
    private void renderUIData(Project project) {
        // 获取持久化数据
        PersistentState persistentState = PersistentStateService.getInstance(project).getState();
        GeneratorProperties generatorProperties = persistentState.getGeneratorContext().getGeneratorProperties();

        // 获取生成配置
        CommonProperties commonProperties = generatorProperties.getCommonProperties();

        // 获取代码生成作者
        authorTf.setText(commonProperties.getAuthor());
        if (StringUtils.isBlank(commonProperties.getAuthor())) {
            authorTf.setText(System.getenv().get("USER"));
        }

        // 框架类型
        FrameworkTypeEnum.getFrameworkNames().forEach(frameworkTypeName -> frameworkTypeComboBox.addItem(frameworkTypeName));
        frameworkTypeComboBox.setSelectedItem(FrameworkTypeEnum.getFrameworkNames().get(0));
        if (StringUtils.isNotBlank(commonProperties.getFrameworkTypeComboBoxValue())) {
            frameworkTypeComboBox.setSelectedItem(commonProperties.getFrameworkTypeComboBoxValue());
        }

        // 项目路径
        projectPathTf.setText(project.getBasePath());

        // 数据库
        databases = commonProperties.getDatabases();
        if (CollectionUtils.isNotEmpty(databases)) {
            databases.forEach(database -> databaseComboBox.addItem(database.getDatabaseName()));

            // 设置默认数据库
            databaseComboBox.setSelectedItem(databases.get(0).getDatabaseName());
            if (StringUtils.isNotBlank(commonProperties.getDatabaseComboBoxValue())) {
                databaseComboBox.setSelectedItem(commonProperties.getDatabaseComboBoxValue());
            }

            for (Database database : databases) {
                if (database.getDatabaseName().equals(databaseComboBox.getSelectedItem())) {
                    selectedDatabase = database;
                }
            }
        }

        // 表名格式
        tableNameRegexTf.setText(commonProperties.getTableNameRegex());

        // entity 设置
        EntityProperties entityProperties = generatorProperties.getEntityProperties();
        entityGenerateCheckBox.setSelected(entityProperties.isSelectedGenerateCheckBox());
        entityPathTf.setText(entityProperties.getPath());
        entityPackageTf.setText(entityProperties.getPackageName());
        entityNamePatternTf.setText(StringUtils.isBlank(entityProperties.getNamePattern()) ? Constant.ENTITY_NAME_DEFAULT_FORMAT : entityProperties.getNamePattern());
        serializableCheckBox.setSelected(entityProperties.isSelectedSerializableCheckBox());
        dataCheckBox.setSelected(entityProperties.isSelectedDataCheckBox());
        builderCheckBox.setSelected(entityProperties.isSelectedBuilderCheckBox());
        noArgsConstructorCheckBox.setSelected(entityProperties.isSelectedNoArgsConstructorCheckBox());
        allArgsConstructorCheckBox.setSelected(entityProperties.isSelectedAllArgsConstructorCheckBox());

        // mapper 设置
        MapperProperties mapperProperties = generatorProperties.getMapperProperties();
        mapperGenerateCheckBox.setSelected(mapperProperties.isSelectedGenerateCheckBox());
        mapperPathTf.setText(mapperProperties.getPath());
        mapperPackageTf.setText(mapperProperties.getPackageName());
        mapperNamePatternTf.setText(StringUtils.isBlank(mapperProperties.getNamePattern()) ? Constant.MAPPER_NAME_DEFAULT_FORMAT : mapperProperties.getNamePattern());

        if (StringUtils.isBlank(mapperProperties.getSuperMapperClass())) {
            if (FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(commonProperties.getFrameworkTypeComboBoxValue())) {
                superMapperClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_MAPPER_CLASS);
            }
            if (FrameworkTypeEnum.TK_MYBATIS.getFrameworkName().equals(commonProperties.getFrameworkTypeComboBoxValue())) {
                superMapperClassTf.setText(Constant.TK_MYBATIS_DEFAULT_SUPER_MAPPER_CLASS);
            }
        } else {
            superMapperClassTf.setText(mapperProperties.getSuperMapperClass());
        }
        enableInsertCheckBox.setSelected(mapperProperties.isSelectedEnableInsertCheckBox());
        enableSelectByPrimaryKeyCheckBox.setSelected(mapperProperties.isSelectedEnableSelectByPrimaryKeyCheckBox());
        enableSelectByExampleCheckBox.setSelected(mapperProperties.isSelectedEnableSelectByExampleCheckBox());
        enableUpdateByPrimaryKeyCheckBox.setSelected(mapperProperties.isSelectedEnableUpdateByPrimaryKeyCheckBox());
        enableUpdateByExampleCheckBox.setSelected(mapperProperties.isSelectedEnableUpdateByExampleCheckBox());
        enableDeleteByPrimaryKeyCheckBox.setSelected(mapperProperties.isSelectedEnableDeleteByPrimaryKeyCheckBox());
        enableDeleteByExampleCheckBox.setSelected(mapperProperties.isSelectedEnableDeleteByExampleCheckBox());
        enableCountByExampleCheckBox.setSelected(mapperProperties.isSelectedEnableCountByExampleCheckBox());

        // mapperXml 设置
        MapperXmlProperties mapperXmlProperties = generatorProperties.getMapperXmlProperties();
        mapperXmlGenerateCheckBox.setSelected(mapperXmlProperties.isSelectedGenerateCheckBox());
        mapperXmlPathTf.setText(mapperXmlProperties.getPath());
        mapperXmlNamePatternTf.setText(StringUtils.isBlank(mapperXmlProperties.getNamePattern()) ? Constant.MAPPER_XML_NAME_DEFAULT_FORMAT : mapperXmlProperties.getNamePattern());

        // service 设置
        ServiceProperties serviceProperties = generatorProperties.getServiceProperties();
        serviceGenerateCheckBox.setSelected(serviceProperties.isSelectedGenerateCheckBox());
        servicePathTf.setText(serviceProperties.getPath());
        servicePackageTf.setText(serviceProperties.getPackageName());
        serviceNamePatternTf.setText(StringUtils.isBlank(serviceProperties.getNamePattern()) ? Constant.SERVICE_NAME_DEFAULT_FORMAT : serviceProperties.getNamePattern());
        if (StringUtils.isBlank(serviceProperties.getSuperServiceClass())
                && FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(commonProperties.getFrameworkTypeComboBoxValue())) {
            superServiceClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_SERVICE_CLASS);
        } else {
            superServiceClassTf.setText(serviceProperties.getSuperServiceClass());
        }

        // serviceImpl 设置
        ServiceImplProperties serviceImplProperties = generatorProperties.getServiceImplProperties();
        serviceImplGenerateCheckBox.setSelected(serviceImplProperties.isSelectedGenerateCheckBox());
        serviceImplPathTf.setText(serviceImplProperties.getPath());
        serviceImplPackageTf.setText(serviceImplProperties.getPackageName());
        serviceImplNamePatternTf.setText(StringUtils.isBlank(serviceImplProperties.getNamePattern()) ? Constant.SERVICE_IMPL_NAME_DEFAULT_FORMAT : serviceImplProperties.getNamePattern());
        if (StringUtils.isBlank(serviceImplProperties.getSuperServiceImplClass())
                && FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(commonProperties.getFrameworkTypeComboBoxValue())) {
            superServiceImplClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_SERVICE_IMPL_CLASS);
        } else {
            superServiceImplClassTf.setText(serviceImplProperties.getSuperServiceImplClass());
        }

        // controller 设置
        ControllerProperties controllerProperties = generatorProperties.getControllerProperties();
        controllerGenerateCheckBox.setSelected(controllerProperties.isSelectedGenerateCheckBox());
        controllerPathTf.setText(controllerProperties.getPath());
        controllerPackageTf.setText(controllerProperties.getPackageName());
        controllerNamePatternTf.setText(StringUtils.isBlank(controllerProperties.getNamePattern()) ? Constant.CONTROLLER_NAME_DEFAULT_FORMAT : controllerProperties.getNamePattern());
        controllerSwaggerCheckBox.setSelected(controllerProperties.isSelectedSwaggerCheckBox());
    }

    /**
     * 重置UI数据
     */
    private void restUIData() {
        // entity 设置
        entityGenerateCheckBox.setSelected(true);
        entityPathTf.setText("");
        entityPackageTf.setText("");
        entityNamePatternTf.setText(Constant.ENTITY_NAME_DEFAULT_FORMAT);
        serviceGenerateCheckBox.setSelected(false);
        dataCheckBox.setSelected(true);
        builderCheckBox.setSelected(false);
        noArgsConstructorCheckBox.setSelected(false);
        allArgsConstructorCheckBox.setSelected(false);

        // mapper 设置
        mapperGenerateCheckBox.setSelected(true);
        mapperPathTf.setText("");
        mapperPackageTf.setText("");
        mapperNamePatternTf.setText(Constant.MAPPER_NAME_DEFAULT_FORMAT);
        superMapperClassTf.setText("");
        if (FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(frameworkTypeComboBox.getSelectedItem())) {
            superMapperClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_MAPPER_CLASS);
        }
        if (FrameworkTypeEnum.TK_MYBATIS.getFrameworkName().equals(frameworkTypeComboBox.getSelectedItem())) {
            superMapperClassTf.setText(Constant.TK_MYBATIS_DEFAULT_SUPER_MAPPER_CLASS);
        }

        enableInsertCheckBox.setSelected(true);
        enableSelectByPrimaryKeyCheckBox.setSelected(true);
        enableSelectByExampleCheckBox.setSelected(false);
        enableUpdateByPrimaryKeyCheckBox.setSelected(true);
        enableUpdateByExampleCheckBox.setSelected(false);
        enableDeleteByPrimaryKeyCheckBox.setSelected(false);
        enableDeleteByExampleCheckBox.setSelected(false);
        enableCountByExampleCheckBox.setSelected(false);

        // mapperXml 设置
        mapperXmlGenerateCheckBox.setSelected(true);
        mapperXmlPathTf.setText("");
        mapperXmlNamePatternTf.setText(Constant.MAPPER_XML_NAME_DEFAULT_FORMAT);

        // service 设置
        serviceGenerateCheckBox.setSelected(false);
        servicePathTf.setText("");
        servicePackageTf.setText("");
        serviceNamePatternTf.setText(Constant.SERVICE_NAME_DEFAULT_FORMAT);
        superServiceClassTf.setText("");
        if (FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(frameworkTypeComboBox.getSelectedItem())) {
            superServiceClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_SERVICE_CLASS);
        }

        // serviceImpl 设置
        serviceImplGenerateCheckBox.setSelected(false);
        serviceImplPathTf.setText("");
        serviceImplPackageTf.setText("");
        serviceImplNamePatternTf.setText(Constant.SERVICE_IMPL_NAME_DEFAULT_FORMAT);
        superServiceImplClassTf.setText("");
        if (FrameworkTypeEnum.MYBATIS_PLUS.getFrameworkName().equals(frameworkTypeComboBox.getSelectedItem())) {
            superServiceImplClassTf.setText(Constant.MYBATIS_PLUS_DEFAULT_SUPER_SERVICE_IMPL_CLASS);
        }

        // controller 设置
        controllerGenerateCheckBox.setSelected(false);
        controllerPathTf.setText("");
        controllerPackageTf.setText("");
        controllerNamePatternTf.setText(Constant.CONTROLLER_NAME_DEFAULT_FORMAT);
        controllerSwaggerCheckBox.setSelected(false);
    }

    /**
     * 创建事件监听器
     *
     * @param project 项目
     */
    private void initActionListener(Project project) {
        entityPathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                entityPathTf.setText(virtualFile.getPath());
            }
        });
        mapperPathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                mapperPathTf.setText(virtualFile.getPath());
            }
        });
        mapperXmlPathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                mapperXmlPathTf.setText(virtualFile.getPath());
            }
        });
        servicePathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                servicePathTf.setText(virtualFile.getPath());
            }
        });
        serviceImplPathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                serviceImplPathTf.setText(virtualFile.getPath());
            }
        });
        controllerPathBtn.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
            if (virtualFile != null) {
                controllerPathTf.setText(virtualFile.getPath());
            }
        });
        databaseComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectedDatabase = databases.stream().filter(database -> database.getDatabaseName().equals(e.getItem())).findAny().get();
            }
        });
        configDataBaseBtn.addActionListener(e -> {
            DataSourcesSettingUI dataSourcesSettingUI = new DataSourcesSettingUI(project, this);
            dataSourcesSettingUI.show();
        });
        selectTableBtn.addActionListener(e -> {
            try {
                MySQLDBHelper dbHelper = new MySQLDBHelper(selectedDatabase);

                // 获取表名列表
                String tableNamePattern = StringUtils.isBlank(tableNameRegexTf.getText()) ? "%" : tableNameRegexTf.getText();
                List<String> tableNames = dbHelper.getTableName(selectedDatabase.getDatabaseName(), tableNamePattern);

                // 显示表名列表
                String[] title = {"", "表名"};
                // 行index,列index
                Object[][] data = new Object[tableNames.size()][2];
                for (int i = 0; i < tableNames.size(); i++) {
                    data[i][1] = tableNames.get(i);
                }

                table.setModel(new DefaultTableModel(data, title));

                // 设置列为单选框
                TableColumn tableColumn = table.getColumnModel().getColumn(0);
                tableColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));
                tableColumn.setCellEditor(table.getDefaultEditor(Boolean.class));
                tableColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
                tableColumn.setMaxWidth(100);
            } catch (Exception ex) {
                Messages.showWarningDialog(project, "数据库连接错误,请检查配置.", "Warning");
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int rowIdx = table.rowAtPoint(e.getPoint());
                    Boolean flag = (Boolean) table.getValueAt(rowIdx, 0);
                    if (Objects.nonNull(flag) && flag) {
                        selectedTableNames.add(table.getValueAt(rowIdx, 1).toString());
                    } else {
                        selectedTableNames.remove(table.getValueAt(rowIdx, 1).toString());
                    }
                }
            }
        });
        restConfigBtn.addActionListener(e -> {
            // 重置UI数据
            restUIData();

            Messages.showWarningDialog(project, "重置成功", "info");
        });
        saveConfigBtn.addActionListener(e -> {
            // 获取代码生成配置
            GeneratorProperties generatorProperties = getGeneratorProperties();
            GeneratorContext generatorContext = new GeneratorContext();
            generatorContext.setGeneratorProperties(generatorProperties);

            // 持久化
            PersistentState persistentState = PersistentStateService.getInstance(project).getState();
            persistentState.setGeneratorContext(generatorContext);

            Messages.showWarningDialog(project, "保存成功", "info");
        });
        generatorBtn.addActionListener(e -> {
            // 获取代码生成配置
            GeneratorProperties generatorProperties = getGeneratorProperties();

            // 获取表列表
            if (CollectionUtils.isEmpty(selectedTableNames)) {
                Messages.showWarningDialog(project, "请选择要生成的表", "info");
                return;
            }
            List<TableInfo> tables = getTables(selectedDatabase, selectedTableNames);
            GeneratorContext generatorContext = new GeneratorContext();
            generatorContext.setTables(tables);
            generatorContext.setGeneratorProperties(generatorProperties);

            // 持久化
            PersistentState persistentState = PersistentStateService.getInstance(project).getState();
            persistentState.setGeneratorContext(generatorContext);

            // 校验数据
            String message = validGeneratorData(generatorProperties);
            if (StringUtils.isNotBlank(message)) {
                Messages.showWarningDialog(project, message, "info");
                return;
            }

            // 生成代码
            generatorService.doGenerator(project, generatorContext);
            Messages.showWarningDialog(project, "代码生成成功", "info");

            // 刷新文件
            LocalFileSystem.getInstance().refreshAndFindFileByPath(project.getBasePath());
        });
        cancelBtn.addActionListener(e -> {
            GeneratorSettingUI.this.dispose();
        });
    }

    /**
     * 获取生成代码配置属性
     *
     * @return 生成代码配置属性
     */
    private GeneratorProperties getGeneratorProperties() {
        GeneratorProperties generatorProperties = new GeneratorProperties();

        // 公共配置
        CommonProperties commonProperties = new CommonProperties();
        commonProperties.setAuthor(authorTf.getText());
        commonProperties.setProjectPath(projectPathTf.getText());
        commonProperties.setDatabases(databases);
        commonProperties.setDatabaseComboBoxValue(String.valueOf(databaseComboBox.getSelectedItem()));
        commonProperties.setTableNameRegex(tableNameRegexTf.getText());
        commonProperties.setFrameworkTypeComboBoxValues(FrameworkTypeEnum.getFrameworkNames());
        commonProperties.setFrameworkTypeComboBoxValue(String.valueOf(frameworkTypeComboBox.getSelectedItem()));
        generatorProperties.setCommonProperties(commonProperties);

        // entity配置
        EntityProperties entityProperties = new EntityProperties();
        entityProperties.setSelectedGenerateCheckBox(entityGenerateCheckBox.isSelected());
        entityProperties.setPath(entityPathTf.getText());
        entityProperties.setPackageName(entityPackageTf.getText());
        entityProperties.setNamePattern(StringUtils.isBlank(entityNamePatternTf.getText()) ? Constant.ENTITY_NAME_DEFAULT_FORMAT : entityNamePatternTf.getText());
        entityProperties.setExampleNamePattern(Constant.ENTITY_EXAMPLE_NAME_DEFAULT_FORMAT);
        entityProperties.setSelectedSerializableCheckBox(serializableCheckBox.isSelected());
        entityProperties.setSelectedDataCheckBox(dataCheckBox.isSelected());
        entityProperties.setSelectedBuilderCheckBox(builderCheckBox.isSelected());
        entityProperties.setSelectedNoArgsConstructorCheckBox(noArgsConstructorCheckBox.isSelected());
        entityProperties.setSelectedAllArgsConstructorCheckBox(allArgsConstructorCheckBox.isSelected());
        generatorProperties.setEntityProperties(entityProperties);

        // mapper配置
        MapperProperties mapperProperties = new MapperProperties();
        mapperProperties.setSelectedGenerateCheckBox(mapperGenerateCheckBox.isSelected());
        mapperProperties.setPath(mapperPathTf.getText());
        mapperProperties.setPackageName(mapperPackageTf.getText());
        mapperProperties.setNamePattern(StringUtils.isBlank(mapperNamePatternTf.getText()) ? Constant.MAPPER_NAME_DEFAULT_FORMAT : mapperNamePatternTf.getText());
        mapperProperties.setSuperMapperClass(superMapperClassTf.getText());
        mapperProperties.setSelectedEnableInsertCheckBox(enableInsertCheckBox.isSelected());
        mapperProperties.setSelectedEnableSelectByPrimaryKeyCheckBox(enableSelectByPrimaryKeyCheckBox.isSelected());
        mapperProperties.setSelectedEnableSelectByExampleCheckBox(enableSelectByExampleCheckBox.isSelected());
        mapperProperties.setSelectedEnableUpdateByPrimaryKeyCheckBox(enableUpdateByPrimaryKeyCheckBox.isSelected());
        mapperProperties.setSelectedEnableUpdateByExampleCheckBox(enableUpdateByExampleCheckBox.isSelected());
        mapperProperties.setSelectedEnableDeleteByPrimaryKeyCheckBox(enableDeleteByPrimaryKeyCheckBox.isSelected());
        mapperProperties.setSelectedEnableDeleteByExampleCheckBox(enableDeleteByExampleCheckBox.isSelected());
        mapperProperties.setSelectedEnableCountByExampleCheckBox(enableCountByExampleCheckBox.isSelected());
        generatorProperties.setMapperProperties(mapperProperties);

        // mapperXml配置
        MapperXmlProperties mapperXmlProperties = new MapperXmlProperties();
        mapperXmlProperties.setSelectedGenerateCheckBox(mapperXmlGenerateCheckBox.isSelected());
        mapperXmlProperties.setPath(mapperXmlPathTf.getText());
        mapperXmlProperties.setNamePattern(mapperXmlNamePatternTf.getText());
        mapperXmlProperties.setPath(mapperXmlPathTf.getText());
        generatorProperties.setMapperXmlProperties(mapperXmlProperties);

        // service配置
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setSelectedGenerateCheckBox(serviceGenerateCheckBox.isSelected());
        serviceProperties.setPath(serviceImplPathTf.getText());
        serviceProperties.setPackageName(servicePackageTf.getText());
        serviceProperties.setNamePattern(StringUtils.isBlank(serviceNamePatternTf.getText()) ? Constant.SERVICE_NAME_DEFAULT_FORMAT : serviceNamePatternTf.getText());
        serviceProperties.setSuperServiceClass(superServiceImplClassTf.getText());
        generatorProperties.setServiceProperties(serviceProperties);

        // serviceImpl配置
        ServiceImplProperties serviceImplProperties = new ServiceImplProperties();
        serviceImplProperties.setSelectedGenerateCheckBox(serviceImplGenerateCheckBox.isSelected());
        serviceImplProperties.setPath(servicePathTf.getText());
        serviceImplProperties.setPackageName(serviceImplPackageTf.getText());
        serviceImplProperties.setNamePattern(StringUtils.isBlank(serviceImplNamePatternTf.getText()) ? Constant.SERVICE_IMPL_NAME_DEFAULT_FORMAT : serviceImplNamePatternTf.getText());
        serviceImplProperties.setSuperServiceImplClass(superServiceImplClassTf.getText());
        generatorProperties.setServiceImplProperties(serviceImplProperties);

        // controller配置
        ControllerProperties controllerProperties = new ControllerProperties();
        controllerProperties.setSelectedGenerateCheckBox(controllerGenerateCheckBox.isSelected());
        controllerProperties.setPath(controllerPathTf.getText());
        controllerProperties.setPackageName(controllerPackageTf.getText());
        controllerProperties.setNamePattern(StringUtils.isBlank(controllerNamePatternTf.getText()) ? Constant.CONTROLLER_NAME_DEFAULT_FORMAT : controllerNamePatternTf.getText());
        controllerProperties.setSelectedSwaggerCheckBox(controllerSwaggerCheckBox.isSelected());
        generatorProperties.setControllerProperties(controllerProperties);

        return generatorProperties;
    }

    /**
     * 校验生成数据
     *
     * @param generatorProperties 生成配置
     * @return 返回非空字符串，校验不通过
     */
    private String validGeneratorData(GeneratorProperties generatorProperties) {
        // 公共配置校验
        CommonProperties commonProperties = generatorProperties.getCommonProperties();
        if (StringUtils.isBlank(commonProperties.getAuthor())) {
            return "请填写生成作者";
        }

        // 实体校验
        EntityProperties entityProperties = generatorProperties.getEntityProperties();
        if (entityProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(entityProperties.getPath())) {
                return "请选择entity路径";
            }
            if (StringUtils.isBlank(entityProperties.getPackageName())) {
                return "请填写entity包名";
            }
            if (!validNamePattern(entityProperties.getNamePattern())) {
                return "entity命名格式需要包含%s";
            }
        }

        // mapper校验
        MapperProperties mapperProperties = generatorProperties.getMapperProperties();
        if (mapperProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(mapperProperties.getPath())) {
                return "请选择mapper路径";
            }
            if (StringUtils.isBlank(mapperProperties.getPackageName())) {
                return "请填写mapper包名";
            }
            if (!validNamePattern(mapperProperties.getNamePattern())) {
                return "mapper命名格式需要包含%s";
            }
        }

        // mapperXml校验
        MapperXmlProperties mapperXmlProperties = generatorProperties.getMapperXmlProperties();
        if (mapperXmlProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(mapperXmlProperties.getPath())) {
                return "请选择mapperXml路径";
            }
            if (!validNamePattern(mapperXmlProperties.getNamePattern())) {
                return "mapperXml命名格式需要包含%s";
            }
        }

        // service校验
        ServiceProperties serviceProperties = generatorProperties.getServiceProperties();
        if (serviceProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(serviceProperties.getPath())) {
                return "请选择service路径";
            }
            if (StringUtils.isBlank(serviceProperties.getPackageName())) {
                return "请填写service包名";
            }
            if (!validNamePattern(serviceProperties.getNamePattern())) {
                return "service命名格式需要包含%s";
            }
        }

        // serviceImpl校验
        ServiceImplProperties serviceImplProperties = generatorProperties.getServiceImplProperties();
        if (serviceImplProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(serviceImplProperties.getPath())) {
                return "请选择serviceImpl路径";
            }
            if (StringUtils.isBlank(serviceImplProperties.getPackageName())) {
                return "请填写serviceImpl包名";
            }
            if (!validNamePattern(serviceImplProperties.getNamePattern())) {
                return "serviceImpl命名格式需要包含%s";
            }
        }

        // controller
        ControllerProperties controllerProperties = generatorProperties.getControllerProperties();
        if (controllerProperties.isSelectedGenerateCheckBox()) {
            if (StringUtils.isBlank(controllerProperties.getPath())) {
                return "请选择controller路径";
            }
            if (StringUtils.isBlank(controllerProperties.getPackageName())) {
                return "请填写controller包名";
            }
            if (!validNamePattern(controllerProperties.getNamePattern())) {
                return "controller命名格式需要包含%s";
            }
        }

        // 表校验
        if (CollectionUtils.isEmpty(selectedTableNames)) {
            return "请选择要生成的表";
        }

        // 至少要生成一种文件
        if (!(entityProperties.isSelectedGenerateCheckBox()
                || mapperProperties.isSelectedGenerateCheckBox()
                || mapperXmlProperties.isSelectedGenerateCheckBox()
                || serviceProperties.isSelectedGenerateCheckBox()
                || serviceImplProperties.isSelectedGenerateCheckBox()
                || controllerProperties.isSelectedGenerateCheckBox())) {
            return "至少要选择生成一种文件";
        }
        return "";
    }

    /**
     * 校验命名格式
     *
     * @param namePattern 命名格式
     * @return true 校验通过、false 校验失败
     */
    private boolean validNamePattern(String namePattern) {
        return StringUtils.isNotBlank(namePattern) && namePattern.contains("%s");
    }

    /**
     * 查询表列表
     *
     * @param database   数据库
     * @param tableNames 表名列表
     * @return 表列表
     */
    private List<TableInfo> getTables(Database database, List<String> tableNames) {
        MySQLDBHelper mySQLDBHelper = new MySQLDBHelper(database);
        List<TableInfo> tables = new ArrayList<>();
        for (String tableName : tableNames) {
            tables.add(mySQLDBHelper.getTableInfo(tableName));
        }
        return tables;
    }

    /**
     * 刷新数据库下拉框
     */
    public void refreshDatabaseComBox(List<Database> databases) {
        String[] databaseNames = new String[databases.size()];
        for (int i = 0; i < databases.size(); i++) {
            databaseNames[i] = databases.get(i).getDatabaseName();
        }

        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(databaseNames);
        databaseComboBox.setModel(comboBoxModel);

        if (databases.size() == 1) {
            selectedDatabase = databases.get(0);
        }
        this.databases = databases;
    }

}
