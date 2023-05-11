import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import { useParams } from "react-router-dom";
import ArticleForm from "main/components/Articles/ArticleForm";
import { Navigate } from "react-router-dom";
import { useBackend, useBackendMutation } from "main/utils/useBackend";
import { toast } from "react-toastify";

export default function ArticlesEditPage() {
    let { id } = useParams();

    const {
        data: article,
        error: error,
        status: status,
    } = useBackend(
        // Stryker disable next-line all : don't test internal caching of React Query
        [`/api/articles?id=${id}`],
        {
            // Stryker disable next-line all : GET is the default, so changing this to "" doesn't introduce a bug
            method: "GET",
            url: `/api/articles`,
            params: {
                id,
            },
        }
    );

    const objectToAxiosPutParams = (article) => ({
        url: "/api/articles",
        method: "PUT",
        params: {
            id: article.id,
        },
        data: {
            title: article.title,
            image: article.image,
            content: article.content,
        },
    });

    const onSuccess = (article) => {
        toast(`Article Updated - id: ${article.id} image: ${article.image}`);
    };

    const mutation = useBackendMutation(
        objectToAxiosPutParams,
        { onSuccess },
        // Stryker disable next-line all : hard to set up test for caching
        [`/api/articles?id=${id}`]
    );

    const { isSuccess } = mutation;

    const onSubmit = async (data) => {
        mutation.mutate(data);
    };

    if (isSuccess) {
        return <Navigate to="/articles/list" />;
    }

    return (
        <BasicLayout>
            <div className="pt-2">
                <h1>Edit Article</h1>
                {article && (
                    <ArticleForm
                        initialArticle={article}
                        submitAction={onSubmit}
                        buttonLabel="Update"
                    />
                )}
            </div>
        </BasicLayout>
    );
}
