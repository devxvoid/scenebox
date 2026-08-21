//
//  AppDirectories.swift
//  SceneBox
//
//  Created by SpontaneousArray on 21.08.26.
//

import Foundation

nonisolated enum AppDirectories {
    // tvOS only allows writes to Caches and tmp.
    static var documents: URL {
        #if os(tvOS)
        FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
        #else
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        #endif
    }

    static var support: URL {
        #if os(tvOS)
        FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
        #else
        FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        #endif
    }
}
