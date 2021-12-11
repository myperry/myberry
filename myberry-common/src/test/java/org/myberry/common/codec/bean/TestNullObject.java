/*
* MIT License
*
* Copyright (c) 2021 MyBerry. All rights reserved.
* https://myberry.org/
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

*   * Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.

*   * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.

*   * Neither the name of MyBerry. nor the names of its contributors may be used
* to endorse or promote products derived from this software without specific
* prior written permission.

* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.myberry.common.codec.bean;

import java.util.List;
import java.util.Set;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;
import org.myberry.common.codec.bean.InnnerObj;

public class TestNullObject implements MessageLite {

  @SerialField(ordinal = 0)
  private int aa = 5;

  @SerialField(ordinal = 1)
  private Integer bb;

  @SerialField(ordinal = 2)
  private Long cc;

  @SerialField(ordinal = 3)
  private Float dd;

  @SerialField(ordinal = 4)
  private Double ee;

  @SerialField(ordinal = 5)
  private double ff;

  @SerialField(ordinal = 6)
  private Boolean gg;

  @SerialField(ordinal = 7)
  private String hh;

  @SerialField(ordinal = 8)
  private InnnerObj ii;

  @SerialField(ordinal = 9)
  private List<Integer> jj;

  @SerialField(ordinal = 10)
  private Set<Integer> kk;

  @SerialField(ordinal = 11)
  private int[] ll;

  @SerialField(ordinal = 12)
  private Integer[] mm;

  @SerialField(ordinal = 13)
  private List<Long> nn;

  @SerialField(ordinal = 14)
  private List<Float> oo;

  @SerialField(ordinal = 15)
  private List<Double> pp;

  @SerialField(ordinal = 16)
  private List<Boolean> qq;

  @SerialField(ordinal = 17)
  private List<String> rr;

  @SerialField(ordinal = 18)
  private Set<String> ss;

  @SerialField(ordinal = 19)
  private String[] tt;

  @SerialField(ordinal = 20)
  private List<InnnerObj> uu;

  @SerialField(ordinal = 21)
  private Set<InnnerObj> vv;

  @SerialField(ordinal = 22)
  private InnnerObj[] ww;

  public int getAa() {
    return aa;
  }

  public void setAa(int aa) {
    this.aa = aa;
  }

  public Integer getBb() {
    return bb;
  }

  public void setBb(Integer bb) {
    this.bb = bb;
  }

  public Long getCc() {
    return cc;
  }

  public void setCc(Long cc) {
    this.cc = cc;
  }

  public Float getDd() {
    return dd;
  }

  public void setDd(Float dd) {
    this.dd = dd;
  }

  public Double getEe() {
    return ee;
  }

  public void setEe(Double ee) {
    this.ee = ee;
  }

  public double getFf() {
    return ff;
  }

  public void setFf(double ff) {
    this.ff = ff;
  }

  public Boolean getGg() {
    return gg;
  }

  public void setGg(Boolean gg) {
    this.gg = gg;
  }

  public String getHh() {
    return hh;
  }

  public void setHh(String hh) {
    this.hh = hh;
  }

  public InnnerObj getIi() {
    return ii;
  }

  public void setIi(InnnerObj ii) {
    this.ii = ii;
  }

  public List<Integer> getJj() {
    return jj;
  }

  public void setJj(List<Integer> jj) {
    this.jj = jj;
  }

  public Set<Integer> getKk() {
    return kk;
  }

  public void setKk(Set<Integer> kk) {
    this.kk = kk;
  }

  public int[] getLl() {
    return ll;
  }

  public void setLl(int[] ll) {
    this.ll = ll;
  }

  public Integer[] getMm() {
    return mm;
  }

  public void setMm(Integer[] mm) {
    this.mm = mm;
  }

  public List<Long> getNn() {
    return nn;
  }

  public void setNn(List<Long> nn) {
    this.nn = nn;
  }

  public List<Float> getOo() {
    return oo;
  }

  public void setOo(List<Float> oo) {
    this.oo = oo;
  }

  public List<Double> getPp() {
    return pp;
  }

  public void setPp(List<Double> pp) {
    this.pp = pp;
  }

  public List<Boolean> getQq() {
    return qq;
  }

  public void setQq(List<Boolean> qq) {
    this.qq = qq;
  }

  public List<String> getRr() {
    return rr;
  }

  public void setRr(List<String> rr) {
    this.rr = rr;
  }

  public Set<String> getSs() {
    return ss;
  }

  public void setSs(Set<String> ss) {
    this.ss = ss;
  }

  public String[] getTt() {
    return tt;
  }

  public void setTt(String[] tt) {
    this.tt = tt;
  }

  public List<InnnerObj> getUu() {
    return uu;
  }

  public void setUu(List<InnnerObj> uu) {
    this.uu = uu;
  }

  public Set<InnnerObj> getVv() {
    return vv;
  }

  public void setVv(Set<InnnerObj> vv) {
    this.vv = vv;
  }

  public InnnerObj[] getWw() {
    return ww;
  }

  public void setWw(InnnerObj[] ww) {
    this.ww = ww;
  }
}
