import React, { useState } from "react";
import { Button, Form } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";

function ArticleForm({ initialArticle, submitAction, buttonLabel = "Create" }) {
    // Stryker disable all
    const {
        register,
        formState: { errors },
        handleSubmit,
    } = useForm({ defaultValues: initialArticle || {} });
    // Stryker enable all

    const navigate = useNavigate();

    return (
        <Form onSubmit={handleSubmit(submitAction)}>
            {initialArticle && (
                <Form.Group className="mb-3">
                    <Form.Label htmlFor="id">Id</Form.Label>
                    <Form.Control
                        data-testid="ArticleForm-id"
                        id="id"
                        type="text"
                        {...register("id")}
                        value={initialArticle.id}
                        disabled
                    />
                </Form.Group>
            )}

            <Form.Group className="mb-3">
                <Form.Label htmlFor="title">Title</Form.Label>
                <Form.Control
                    data-testid="ArticleForm-title"
                    id="title"
                    type="text"
                    isInvalid={Boolean(errors.title)}
                    {...register("title", {
                        required: true,
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.title && "Title is required. "}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label htmlFor="image">Image</Form.Label>
                <Form.Control
                    data-testid="ArticleForm-image"
                    id="image"
                    type="text"
                    isInvalid={Boolean(errors.image)}
                    {...register("image", {
                        required: "Image is required.",
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.image?.message}
                </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
                <Form.Label htmlFor="content">Content</Form.Label>
                <Form.Control
                    data-testid="ArticleForm-content"
                    id="content"
                    type="text"
                    isInvalid={Boolean(errors.content)}
                    {...register("content", {
                        required: true,
                    })}
                />
                <Form.Control.Feedback type="invalid">
                    {errors.content && "Content is required. "}
                </Form.Control.Feedback>
            </Form.Group>

            <Button type="submit" data-testid="ArticleForm-submit">
                {buttonLabel}
            </Button>
            <Button
                variant="Secondary"
                onClick={() => navigate(-1)}
                data-testid="ArticleForm-cancel"
            >
                Cancel
            </Button>
        </Form>
    );
}

export default ArticleForm;
