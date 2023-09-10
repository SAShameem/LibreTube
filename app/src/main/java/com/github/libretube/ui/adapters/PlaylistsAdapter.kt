package com.github.libretube.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.libretube.R
import com.github.libretube.api.obj.Playlists
import com.github.libretube.constants.IntentData
import com.github.libretube.databinding.PlaylistsRowBinding
import com.github.libretube.enums.PlaylistType
import com.github.libretube.helpers.ImageHelper
import com.github.libretube.helpers.NavigationHelper
import com.github.libretube.ui.base.BaseActivity
import com.github.libretube.ui.sheets.PlaylistOptionsBottomSheet
import com.github.libretube.ui.viewholders.PlaylistsViewHolder

class PlaylistsAdapter(
    private val playlists: MutableList<Playlists>,
    private val playlistType: PlaylistType
) : RecyclerView.Adapter<PlaylistsViewHolder>() {

    override fun getItemCount() = playlists.size

    fun updateItems(newItems: List<Playlists>) {
        val oldSize = playlists.size
        playlists.addAll(newItems)
        notifyItemRangeInserted(oldSize, playlists.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PlaylistsRowBinding.inflate(layoutInflater, parent, false)
        return PlaylistsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.binding.apply {
            // set imageview drawable as empty playlist if imageview empty
            if (playlist.thumbnail.orEmpty().split("/").size <= 4) {
                playlistThumbnail.setImageResource(R.drawable.ic_empty_playlist)
                playlistThumbnail
                    .setBackgroundColor(com.google.android.material.R.attr.colorSurface)
            } else {
                ImageHelper.loadImage(playlist.thumbnail, playlistThumbnail)
            }
            playlistTitle.text = playlist.name

            videoCount.text = playlist.videos.toString()

            root.setOnClickListener {
                NavigationHelper.navigatePlaylist(root.context, playlist.id, playlistType)
            }

            val fragmentManager = (root.context as BaseActivity).supportFragmentManager
            root.setOnLongClickListener {
                fragmentManager.setFragmentResultListener(
                    PLAYLISTS_ADAPTER_REQUEST_KEY,
                    (root.context as BaseActivity)
                ) { _, resultBundle ->
                    val newPlaylistDescription =
                        resultBundle.getString(IntentData.playlistDescription)
                    val newPlaylistName =
                        resultBundle.getString(IntentData.playlistName)
                    val isPlaylistToBeDeleted =
                        resultBundle.getBoolean(IntentData.playlistTask)

                    newPlaylistDescription?.let {
                        playlistDescription.text = it
                        playlist.shortDescription = it
                    }

                    newPlaylistName?.let {
                        playlistTitle.text = it
                        playlist.name = it
                    }

                    if (isPlaylistToBeDeleted) {
                        // try to refresh the playlists in the library on deletion success
                        onDelete(position, root.context as BaseActivity)
                    }
                }

                val playlistOptionsDialog = PlaylistOptionsBottomSheet(
                    playlistId = playlist.id!!,
                    playlistName = playlist.name!!,
                    playlistType = playlistType
                )
                playlistOptionsDialog.show(
                    fragmentManager,
                    PlaylistOptionsBottomSheet::class.java.name
                )
                true
            }
        }
    }

    private fun onDelete(position: Int, activity: BaseActivity) {
        playlists.removeAt(position)
        activity.runOnUiThread {
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    companion object {
        const val PLAYLISTS_ADAPTER_REQUEST_KEY = "playlists_adapter_request_key"
    }
}
