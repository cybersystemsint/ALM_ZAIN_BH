package com.zain.bh.alm.acceptance.dto;

public class ItemCodeSubstituteDTO {
	private Long recordNo;
	private String itemCode;
	private String relatedItemCode;
	private String reciprocalFlag;
	private String createdBy;
	private String updatedBy;

	public Long getRecordNo() {
		return recordNo;
	}

	public void setRecordNo(Long recordNo) {
		this.recordNo = recordNo;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getRelatedItemCode() {
		return relatedItemCode;
	}

	public void setRelatedItemCode(String relatedItemCode) {
		this.relatedItemCode = relatedItemCode;
	}

	public String getReciprocalFlag() {
		return reciprocalFlag;
	}

	public void setReciprocalFlag(String reciprocalFlag) {
		this.reciprocalFlag = reciprocalFlag;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
