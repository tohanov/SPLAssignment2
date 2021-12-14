package bgu.spl.mics.application.objects;

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


    private Type type;
    private int processed;
	private int trained;
    private int size;


    public Data(Type type, /*int processed,*/ int size){

        this.type=type;
        this.processed=0; // TODO: fix
        this.size=size;

    }


    public Type getType(){

        return type;
    }


    public int getProcessed(){

        return processed;
    }


    public int getSize(){

        return size;
    }

    public boolean isProcessed(){

        return processed == size;
    }

    public void increaseNumOfProcessedSamples(int number_of_processed_samples_to_add){
        processed+=number_of_processed_samples_to_add;

    // extreme case    
        if(processed>size)
            processed=size;

    }


	public boolean isTrained(){

        return processed == size;
    }


	public static Data.Type typeFromString(String _strType) {
		Data.Type retType;
		String lowerCaseType = _strType.toLowerCase();

		if (lowerCaseType == "images") retType = Type.Images;
		else if (lowerCaseType == "text") retType = Type.Text;
		else retType = Type.Tabular;

		return retType;
	}
}
