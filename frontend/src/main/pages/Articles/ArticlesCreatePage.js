import BasicLayout from "main/layouts/BasicLayout/BasicLayout";
import ArticleForm from "main/components/Articles/ArticleForm";
import { Navigate } from "react-router-dom";
import { useBackendMutation } from "main/utils/useBackend";
import { toast } from "react-toastify";

export default function ArticlesCreatePage() {
    const objectToAxiosParams = (article) => ({
        url: "/api/articles/post",
        method: "POST",
        params: {
            title: article.title,
            image: article.image,
            content: article.content,
        },
    });

    const onSuccess = (article) => {
        toast(
            `New article Created - id: ${article.id} image: ${article.image}`
        );
    };

    const mutation = useBackendMutation(
        objectToAxiosParams,
        { onSuccess },
        // Stryker disable next-line all : hard to set up test for caching
        ["/api/articles/all"]
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
                <h1>Create New Article</h1>

                <ArticleForm submitAction={onSubmit} />
            </div>
        </BasicLayout>
    );
}
