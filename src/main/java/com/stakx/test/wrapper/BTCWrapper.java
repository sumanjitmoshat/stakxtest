package com.stakx.test.wrapper;

import java.math.BigDecimal;

public class BTCWrapper {

	private boolean flag;
	private BigDecimal BTCVolume;

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public BigDecimal getBTCVolume() {
		return BTCVolume;
	}

	public void setBTCVolume(BigDecimal bTCVolume) {
		BTCVolume = bTCVolume;
	}

	public BTCWrapper(boolean flag, BigDecimal bTCVolume) {
		super();
		this.flag = flag;
		BTCVolume = bTCVolume;
	}

	public BTCWrapper() {
		super();
	}

}
