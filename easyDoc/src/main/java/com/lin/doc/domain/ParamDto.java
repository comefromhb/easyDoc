package com.lin.doc.domain;

public class ParamDto {
    /**
     * 名称
     */
    private String name;
    /**
     * 代码
     */
    private String code;
    /**
     * 类型
     */
    private String type;
    /**
     * 是否必填
     */
    private String require;
    /**
     * 父节点
     */
    private String parentNode;
    /**
     * 描述
     */
    private String description;
    /**
     * 层级
     */
    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRequire() {
        return require;
    }

    public void setRequire(String require) {
        this.require = require;
    }

    public String getParentNode() {
        return parentNode;
    }

    public void setParentNode(String parentNode) {
        this.parentNode = parentNode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ParamDto{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", type='" + type + '\'' +
                ", require='" + require + '\'' +
                ", parentNode='" + parentNode + '\'' +
                ", description='" + description + '\'' +
                ", level=" + level +
                '}';
    }
}
