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

public class TestObject implements MessageLite {

  @SerialField(ordinal = 0)
  private int a;

  @SerialField(ordinal = 1)
  private Integer b;

  @SerialField(ordinal = 2)
  private long c;

  @SerialField(ordinal = 3)
  private Long d;

  @SerialField(ordinal = 4)
  private float e;

  @SerialField(ordinal = 5)
  private Float f;

  @SerialField(ordinal = 6)
  private double g;

  @SerialField(ordinal = 7)
  private Double h;

  @SerialField(ordinal = 8)
  private boolean i;

  @SerialField(ordinal = 9)
  private Boolean j;

  @SerialField(ordinal = 10)
  private String k;

  @SerialField(ordinal = 11)
  private List<Integer> l;

  @SerialField(ordinal = 12)
  private Set<Integer> m;

  @SerialField(ordinal = 13)
  private int[] n;

  @SerialField(ordinal = 14)
  private Integer[] o;

  @SerialField(ordinal = 15)
  private List<Long> p;

  @SerialField(ordinal = 16)
  private Set<Long> q;

  @SerialField(ordinal = 17)
  private long[] r;

  @SerialField(ordinal = 18)
  private Long[] s;

  @SerialField(ordinal = 19)
  private List<Float> t;

  @SerialField(ordinal = 20)
  private Set<Float> u;

  @SerialField(ordinal = 21)
  private float[] v;

  @SerialField(ordinal = 22)
  private Float[] w;

  @SerialField(ordinal = 23)
  private List<Double> x;

  @SerialField(ordinal = 24)
  private Set<Double> y;

  @SerialField(ordinal = 25)
  private double[] z;

  @SerialField(ordinal = 26)
  private Double[] aa;

  @SerialField(ordinal = 27)
  private List<Boolean> bb;

  @SerialField(ordinal = 28)
  private Set<Boolean> cc;

  @SerialField(ordinal = 29)
  private boolean[] dd;

  @SerialField(ordinal = 30)
  private Boolean[] ee;

  @SerialField(ordinal = 31)
  private List<String> ff;

  @SerialField(ordinal = 32)
  private Set<String> gg;

  @SerialField(ordinal = 33)
  private String[] hh;

  @SerialField(ordinal = 34)
  private InnnerObj innnerObj;

  @SerialField(ordinal = 35)
  private List<InnnerObj> innnerObjList;

  @SerialField(ordinal = 36)
  private Set<InnnerObj> innnerObjSet;

  @SerialField(ordinal = 37)
  private InnnerObj[] innnerObjArray;

  public int getA() {
    return a;
  }

  public void setA(int a) {
    this.a = a;
  }

  public Integer getB() {
    return b;
  }

  public void setB(Integer b) {
    this.b = b;
  }

  public long getC() {
    return c;
  }

  public void setC(long c) {
    this.c = c;
  }

  public Long getD() {
    return d;
  }

  public void setD(Long d) {
    this.d = d;
  }

  public float getE() {
    return e;
  }

  public void setE(float e) {
    this.e = e;
  }

  public Float getF() {
    return f;
  }

  public void setF(Float f) {
    this.f = f;
  }

  public double getG() {
    return g;
  }

  public void setG(double g) {
    this.g = g;
  }

  public Double getH() {
    return h;
  }

  public void setH(Double h) {
    this.h = h;
  }

  public boolean isI() {
    return i;
  }

  public void setI(boolean i) {
    this.i = i;
  }

  public Boolean getJ() {
    return j;
  }

  public void setJ(Boolean j) {
    this.j = j;
  }

  public String getK() {
    return k;
  }

  public void setK(String k) {
    this.k = k;
  }

  public List<Integer> getL() {
    return l;
  }

  public void setL(List<Integer> l) {
    this.l = l;
  }

  public Set<Integer> getM() {
    return m;
  }

  public void setM(Set<Integer> m) {
    this.m = m;
  }

  public int[] getN() {
    return n;
  }

  public void setN(int[] n) {
    this.n = n;
  }

  public Integer[] getO() {
    return o;
  }

  public void setO(Integer[] o) {
    this.o = o;
  }

  public List<Long> getP() {
    return p;
  }

  public void setP(List<Long> p) {
    this.p = p;
  }

  public Set<Long> getQ() {
    return q;
  }

  public void setQ(Set<Long> q) {
    this.q = q;
  }

  public long[] getR() {
    return r;
  }

  public void setR(long[] r) {
    this.r = r;
  }

  public Long[] getS() {
    return s;
  }

  public void setS(Long[] s) {
    this.s = s;
  }

  public List<Float> getT() {
    return t;
  }

  public void setT(List<Float> t) {
    this.t = t;
  }

  public Set<Float> getU() {
    return u;
  }

  public void setU(Set<Float> u) {
    this.u = u;
  }

  public float[] getV() {
    return v;
  }

  public void setV(float[] v) {
    this.v = v;
  }

  public Float[] getW() {
    return w;
  }

  public void setW(Float[] w) {
    this.w = w;
  }

  public List<Double> getX() {
    return x;
  }

  public void setX(List<Double> x) {
    this.x = x;
  }

  public Set<Double> getY() {
    return y;
  }

  public void setY(Set<Double> y) {
    this.y = y;
  }

  public double[] getZ() {
    return z;
  }

  public void setZ(double[] z) {
    this.z = z;
  }

  public Double[] getAa() {
    return aa;
  }

  public void setAa(Double[] aa) {
    this.aa = aa;
  }

  public List<Boolean> getBb() {
    return bb;
  }

  public void setBb(List<Boolean> bb) {
    this.bb = bb;
  }

  public Set<Boolean> getCc() {
    return cc;
  }

  public void setCc(Set<Boolean> cc) {
    this.cc = cc;
  }

  public boolean[] getDd() {
    return dd;
  }

  public void setDd(boolean[] dd) {
    this.dd = dd;
  }

  public Boolean[] getEe() {
    return ee;
  }

  public void setEe(Boolean[] ee) {
    this.ee = ee;
  }

  public List<String> getFf() {
    return ff;
  }

  public void setFf(List<String> ff) {
    this.ff = ff;
  }

  public Set<String> getGg() {
    return gg;
  }

  public void setGg(Set<String> gg) {
    this.gg = gg;
  }

  public String[] getHh() {
    return hh;
  }

  public void setHh(String[] hh) {
    this.hh = hh;
  }

  public InnnerObj getInnnerObj() {
    return innnerObj;
  }

  public void setInnnerObj(InnnerObj innnerObj) {
    this.innnerObj = innnerObj;
  }

  public List<InnnerObj> getInnnerObjList() {
    return innnerObjList;
  }

  public void setInnnerObjList(List<InnnerObj> innnerObjList) {
    this.innnerObjList = innnerObjList;
  }

  public Set<InnnerObj> getInnnerObjSet() {
    return innnerObjSet;
  }

  public void setInnnerObjSet(Set<InnnerObj> innnerObjSet) {
    this.innnerObjSet = innnerObjSet;
  }

  public InnnerObj[] getInnnerObjArray() {
    return innnerObjArray;
  }

  public void setInnnerObjArray(InnnerObj[] innnerObjArray) {
    this.innnerObjArray = innnerObjArray;
  }
}
