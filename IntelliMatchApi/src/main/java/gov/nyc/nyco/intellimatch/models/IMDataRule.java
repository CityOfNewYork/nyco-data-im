package gov.nyc.nyco.intellimatch.models;

import com.opencsv.bean.CsvBindByPosition;

import lombok.Data;

@Data
public class IMDataRule {
	
	@CsvBindByPosition(position = 0)
	private String fieldName;
	@CsvBindByPosition(position = 1)
	private String maxSize;
	@CsvBindByPosition(position = 2)
	private String vaidationRule;
	@CsvBindByPosition(position = 3)
	private String isFieldRequired;
	@CsvBindByPosition(position = 4)
	private Integer fieldNumber;
	@CsvBindByPosition(position = 5)
	private String vaidationRuleRegex;

	public IMDataRule() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IMDataRule(String fieldName, String maxSize, String vaidationRule, String vaidationRuleRegex,
			Integer fieldNumber, String isFieldRequired) {
		super();
		this.fieldName = fieldName;
		this.maxSize = maxSize;
		this.vaidationRule = vaidationRule;
		this.vaidationRuleRegex = vaidationRuleRegex;
		this.fieldNumber = fieldNumber;
		this.isFieldRequired = isFieldRequired;
	}

	// Matches
	public String matches(String value) {
		Integer max = new Integer(maxSize);

		// 1. value is not required
		boolean isValueRequired = (value == null || value.isEmpty()) 
				&& (isFieldRequired != null && !isFieldRequired.isEmpty());
		
		if(isValueRequired) {
			// return "[" + fieldName + "] is required field. : " + isFieldRequired;
			return "[" + fieldName + "] is required field.";
		}
		
		// 2. the length of value exceeded maxsize
		boolean isLength = 
				value == null || value.isEmpty() || value.length() <= max  ;
		
		if(!isLength) {
			return "[" + fieldName + "] is exceeded max length : " + max ;
		}
		
		// 3. matches regular expression
		boolean isMatch	= (vaidationRuleRegex==null) 
				|| (vaidationRuleRegex.isEmpty())
				|| ( !isValueRequired && (value==null || value.isEmpty()) )
				|| ((value!=null && !value.isEmpty())?value.matches(vaidationRuleRegex):false);
		if(!isMatch) {
			// return "[" + fieldName + "] " + vaidationRule + " " + value + " does not match " + vaidationRuleRegex;
			return "[" + fieldName + "] " + vaidationRule + " does not match.";
		}

		return "";
		
	}
	 
}
