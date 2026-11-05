@@ -228,24 +228,22 @@ private boolean handleExternalUrl(Uri uri) {

    private void openUpi(Uri uri) {

        try {

            Intent intent =
                    new Intent(Intent.ACTION_VIEW);
    try {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

            intent.setData(uri);

            startActivity(intent);
        startActivity(intent);

        } catch (ActivityNotFoundException e) {
    } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "UPI app install pannirukkingala check pannunga",
                    Toast.LENGTH_LONG
            ).show();
        }
        Toast.makeText(
                this,
                "GPay / PhonePe / Paytm போன்ற UPI app இல்லை",
                Toast.LENGTH_LONG
        ).show();
    }
}

    private void openIntentUri(Uri uri) {
