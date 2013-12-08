package name.fraser.neil.plaintext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

import static name.fraser.neil.plaintext.diff_match_patch.unescapeForEncodeUriCompatability;

/**
 * Class representing one patch operation.
 */
public class Patch {
  private LinkedList<Diff> diffs;
  private int start1;
  private int start2;
  private int length1;
  private int length2;

  /**
   * Constructor.  Initializes with an empty list of diffs.
   */
  public Patch() {
    this.diffs = new LinkedList<Diff>();
  }

  /**
   * Emmulate GNU diff's format.
   * Header: @@ -382,8 +481,9 @@
   * Indicies are printed as 1-based, not 0-based.
   * @return The GNU diff string.
   */
  public String toString() {
    String coords1, coords2;
    if (this.length1 == 0) {
      coords1 = this.start1 + ",0";
    } else if (this.length1 == 1) {
      coords1 = Integer.toString(this.start1 + 1);
    } else {
      coords1 = (this.start1 + 1) + "," + this.length1;
    }
    if (this.length2 == 0) {
      coords2 = this.start2 + ",0";
    } else if (this.length2 == 1) {
      coords2 = Integer.toString(this.start2 + 1);
    } else {
      coords2 = (this.start2 + 1) + "," + this.length2;
    }
    StringBuilder text = new StringBuilder();
    text.append("@@ -").append(coords1).append(" +").append(coords2)
        .append(" @@\n");
    // Escape the body of the patch with %xx notation.
    for (Diff aDiff : this.diffs) {
      switch (aDiff.getOperation()) {
      case INSERT:
        text.append('+');
        break;
      case DELETE:
        text.append('-');
        break;
      case EQUAL:
        text.append(' ');
        break;
      }
      try {
        text.append(URLEncoder.encode(aDiff.getText(), "UTF-8").replace('+', ' '))
            .append("\n");
      } catch (UnsupportedEncodingException e) {
        // Not likely on modern system.
        throw new Error("This system does not support UTF-8.", e);
      }
    }
    return unescapeForEncodeUriCompatability(text.toString());
  }
  
  public int getStart1() {
	  return start1;
  }
  public int getLength1() {
	  return length1;
  }
  public int getStart2() {
	  return start2;
  }
  public int getLength2() {
	  return length2;
  }
  public LinkedList<Diff> getDiffs() {
	  return diffs;
  }
  
  public void setStart1(int start1) {
	  this.start1 = start1;
  }
  public void setLength1(int length1) {
	  this.length1 = length1;
  }
  public void setStart2(int start2) {
	  this.start2 = start2;
  }
  public void setLength2(int length2) {
	  this.length2 = length2;
  }
  
  public void incStart1(int delta) {
	  this.start1 += delta;
  }
  public void incLength1(int delta) {
	  this.length1 += delta;
  }
  public void incStart2(int delta) {
	  this.start2 += delta;
  }
  public void incLength2(int delta) {
	  this.length2 += delta;
  }
}