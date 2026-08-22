//
//  GuestMigration.swift
//  SceneBox
//
//  Created by SpontaneousArray on 22.08.26.
//

import Foundation

enum GuestMigration {
    static func migrate(uid: String, profileID: String) async {
        let localProgress = LocalWatchProgressBackend()
        let localWatchlist = LocalWatchlistBackend()
        let progress = FirestoreWatchProgressBackend(uid: uid, profileID: profileID)
        let watchlist = FirestoreWatchlistBackend(uid: uid, profileID: profileID)

        for item in await localProgress.load() { await progress.upsert(item) }
        for item in await localWatchlist.load() { await watchlist.upsert(item) }

        await localProgress.clear()
        for item in await localWatchlist.load() { await localWatchlist.remove(id: item.id) }
    }
}
