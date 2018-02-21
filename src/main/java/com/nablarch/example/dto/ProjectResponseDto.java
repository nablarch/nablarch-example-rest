package com.nablarch.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nablarch.example.entity.Client;
import com.nablarch.example.entity.SystemAccount;
import nablarch.core.text.FormatterUtil;
import nablarch.core.util.DateUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * プロジェクト
 */
public class ProjectResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** プロジェクトID */
    private Integer projectId;

    /** プロジェクト名 */
    private String projectName;

    /** プロジェクト種別 */
    private String projectType;

    /** プロジェクト分類 */
    private String projectClass;

    /** プロジェクト開始日付 */
    private Date projectStartDate;

    /** プロジェクト終了日付 */
    private Date projectEndDate;

    /** 顧客ID */
    private Integer clientId;

    /** プロジェクトマネージャー */
    private String projectManager;

    /** プロジェクトリーダー */
    private String projectLeader;

    /** ユーザID */
    private Integer userId;

    /** 備考 */
    private String note;

    /** 売上高 */
    private Integer sales;

    /** 売上原価 */
    private Integer costOfGoodsSold;

    /** 販管費 */
    private Integer sga;

    /** 本社配賦 */
    private Integer allocationOfCorpExpenses;

    /** client関連プロパティ */
    private Client client;

    /** systemAccount関連プロパティ */
    private SystemAccount systemAccount;
    /**
     * プロジェクトIDを返します。
     *
     * @return プロジェクトID
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * プロジェクトIDを設定します。
     *
     * @param projectId プロジェクトID
     */
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
    /**
     * プロジェクト名を返します。
     *
     * @return プロジェクト名
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * プロジェクト名を設定します。
     *
     * @param projectName プロジェクト名
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    /**
     * プロジェクト種別を返します。
     *
     * @return プロジェクト種別
     */
    public String getProjectType() {
        return projectType;
    }

    /**
     * プロジェクト種別を設定します。
     *
     * @param projectType プロジェクト種別
     */
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }
    /**
     * プロジェクト分類を返します。
     *
     * @return プロジェクト分類
     */
    public String getProjectClass() {
        return projectClass;
    }

    /**
     * プロジェクト分類を設定します。
     *
     * @param projectClass プロジェクト分類
     */
    public void setProjectClass(String projectClass) {
        this.projectClass = projectClass;
    }
    /**
     * プロジェクト開始日付を返します。
     *
     * @return プロジェクト開始日付
     */
    @JsonIgnore
    public Date getProjectStartDate() {
        return projectStartDate;
    }

    /**
     * プロジェクト開始日付を設定します。
     *
     * @param projectStartDate プロジェクト開始日付
     */
    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    /**
     * 文字列に変換したプロジェクト開始日付を返します。
     *
     * @return プロジェクト開始日付（文字列）
     */
    @JsonProperty("projectStartDate")
    public String getProjectStartDateString() {
        String ret = null;
        if (projectStartDate != null) {
            ret = FormatterUtil.format("dateTime", projectStartDate);
        }
        return ret;
    }

    /**
     * 文字列でプロジェクト開始日付を設定します。
     *
     * @param projectStartDate プロジェクト開始日付（文字列）
     */
    public void setProjectStartDateString(String projectStartDate) {
        if(projectStartDate != null){
            this.projectStartDate = DateUtil.getParsedDate(projectStartDate,"yyyy/MM/dd");
        }else {
            this.projectStartDate = null;
        }
    }

    /**
     * プロジェクト終了日付を返します。
     *
     * @return プロジェクト終了日付
     */
    @JsonIgnore
    public Date getProjectEndDate() {
        return projectEndDate;
    }

    /**
     * プロジェクト終了日付を設定します。
     *
     * @param projectEndDate プロジェクト終了日付
     */
    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
    }

    /**
     * 文字列に変換したプロジェクト終了日付を返します。
     *
     * @return プロジェクト終了日付（文字列）
     */
    @JsonProperty("projectEndDate")
    public String getProjectEndDateString() {
        String ret = null;
        if (projectEndDate != null) {
            ret = FormatterUtil.format("dateTime", projectEndDate);
        }
        return ret;
    }

    /**
     * 文字列でプロジェクト終了日付を設定します。
     *
     * @param projectEndDate プロジェクト終了日付（文字列）
     */
    public void setProjectEndDateString(String projectEndDate) {
        if(projectEndDate != null){
            this.projectEndDate = DateUtil.getParsedDate(projectEndDate,"yyyy/MM/dd");
        }else {
            this.projectEndDate = null;
        }
    }


    /**
     * 顧客IDを返します。
     *
     * @return 顧客ID
     */
    public Integer getClientId() {
        return clientId;
    }

    /**
     * 顧客IDを設定します。
     *
     * @param clientId 顧客ID
     */
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    /**
     * プロジェクトマネージャーを返します。
     *
     * @return プロジェクトマネージャー
     */
    public String getProjectManager() {
        return projectManager;
    }

    /**
     * プロジェクトマネージャーを設定します。
     *
     * @param projectManager プロジェクトマネージャー
     */
    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }
    /**
     * プロジェクトリーダーを返します。
     *
     * @return プロジェクトリーダー
     */
    public String getProjectLeader() {
        return projectLeader;
    }

    /**
     * プロジェクトリーダーを設定します。
     *
     * @param projectLeader プロジェクトリーダー
     */
    public void setProjectLeader(String projectLeader) {
        this.projectLeader = projectLeader;
    }
    /**
     * ユーザIDを返します。
     *
     * @return ユーザID
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * ユーザIDを設定します。
     *
     * @param userId ユーザID
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    /**
     * 備考を返します。
     *
     * @return 備考
     */
    public String getNote() {
        return note;
    }

    /**
     * 備考を設定します。
     *
     * @param note 備考
     */
    public void setNote(String note) {
        this.note = note;
    }
    /**
     * 売上高を返します。
     *
     * @return 売上高
     */
    public Integer getSales() {
        return sales;
    }

    /**
     * 売上高を設定します。
     *
     * @param sales 売上高
     */
    public void setSales(Integer sales) {
        this.sales = sales;
    }
    /**
     * 売上原価を返します。
     *
     * @return 売上原価
     */
    public Integer getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    /**
     * 売上原価を設定します。
     *
     * @param costOfGoodsSold 売上原価
     */
    public void setCostOfGoodsSold(Integer costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }
    /**
     * 販管費を返します。
     *
     * @return 販管費
     */
    public Integer getSga() {
        return sga;
    }

    /**
     * 販管費を設定します。
     *
     * @param sga 販管費
     */
    public void setSga(Integer sga) {
        this.sga = sga;
    }

    /**
     * 本社配賦を返します。
     *
     * @return 本社配賦
     */
    public Integer getAllocationOfCorpExpenses() {
        return allocationOfCorpExpenses;
    }

    /**
     * 本社配賦を設定します。
     *
     * @param allocationOfCorpExpenses 本社配賦
     */
    public void setAllocationOfCorpExpenses(Integer allocationOfCorpExpenses) {
        this.allocationOfCorpExpenses = allocationOfCorpExpenses;
    }

    /**
     * clientを返します。
     *
     * @return client
     */
    public Client getClient() {
        return client;
    }

    /**
     * clientを設定します。
     *
     * @param client client
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * systemAccountを返します。
     *
     * @return systemAccount
     */
    public SystemAccount getSystemAccount() {
        return systemAccount;
    }

    /**
     * systemAccountを設定します。
     *
     * @param systemAccount systemAccount
     */
    public void setSystemAccount(SystemAccount systemAccount) {
        this.systemAccount = systemAccount;
    }
}
