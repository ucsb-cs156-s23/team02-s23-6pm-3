import React from "react";
import { useBackend } from "main/utils/useBackend";

import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import ArticlesTable from "main/components/Articles/ArticlesTable";
import { useCurrentUser } from "main/utils/currentUser";

export default function ArticlesIndexPage() {
    const currentUser = useCurrentUser();

    const {
        data: articles,
        error: _error,
        status: _status,
    } = useBackend(
        // Stryker disable next-line all : don't test internal caching of React Query
        ["/api/articles/all"],
        { method: "GET", url: "/api/articles/all" },
        []
    );

    return (
        <BasicLayout>
            <div className="pt-2">
                <h1>Articles</h1>
                <ArticlesTable articles={articles} currentUser={currentUser} />
            </div>
        </BasicLayout>
    );
}
