package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;


/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {

    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }

    @Expose private Type type;
    private int processed;
    @Expose private int size;


	public Data(Type type, int size) {
		this.type = type;
		this.processed = 0;
		this.size = size;
	}


	public Type getType() {
		return type;
	}


	public int getProcessed() {
		return processed;
	}


	public int getSize() {
		return size;
	}


	public boolean isProcessed() {
		return processed == size;
	}


	public void increaseNumOfProcessedSamples(int number_of_processed_samples_to_add) {
		processed += number_of_processed_samples_to_add;

		// outlier case
		if (processed > size)
			processed = size;
	}


	public boolean isTrained() {
		return processed == size;
	}


	public static Data.Type typeFromString(String _strType) {
		Data.Type retType;
		String lowerCaseType = _strType.toLowerCase();

		if (lowerCaseType.equals("images"))
			retType = Type.Images;
		else if (lowerCaseType.equals("text"))
			retType = Type.Text;
		else // "tabular"
			retType = Type.Tabular;

		return retType;
	}
}
