package com.example.shoppingcart

import android.net.Uri

class ModelImagePicked {
    var id = ""
    var imageUri: Uri? = null
    var imageUr: String? = null
    var fromInternet = false//this will be used to show images (picked from gallery/camera - false or from FB -true) in AdCreateActivity

    constructor()


    constructor(id: String, imageUri: Uri?, imageUr: String?, fromInternet: Boolean) {
        this.id = id
        this.imageUri = imageUri
        this.imageUr = imageUr
        this.fromInternet = fromInternet
    }
}