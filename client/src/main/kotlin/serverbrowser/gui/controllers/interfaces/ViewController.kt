package serverbrowser.gui.controllers.interfaces

/**
 * Interface that provides the minimal necessary methods for ViewController implementations.
 *
 * @author Marcel
 */
interface ViewController {
    /**
     * This method is called by the FXMLLoader after loading the FXML File.
     */
    fun initialize() {
        // Do nothing by default
    }

    /**
     * This method is called, as soon as the View closes.
     */
    fun onClose() {
        // Do nothing by default
    }
}
